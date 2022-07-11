package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.optimizer.Optimization
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max

typealias AddCommand = (Command) -> Unit

@Serializable
@SerialName("BodyInfoChange")
data class BodyInfoChange(
	val oldInfo: BodyInfo,
	val newInfo: BodyInfo,
)

@Serializable
sealed class Command {
	abstract val optimization: Optimization
	abstract val bodyInfoChange: BodyInfoChange?
	abstract fun apply(program: Program): Program
	abstract fun revert(program: Program): Program?

}

@Serializable
@SerialName("AddBasicStatement")
data class AddBasicStatement(
	override val optimization: Optimization,
	override val bodyInfoChange: BodyInfoChange?,
	val address: BasicStatementAddress,
	val toAdd: BasicStatement,
) : Command() {
	override fun apply(program: Program) = CommandHelper.addBasicStatement(program, bodyInfoChange?.newInfo, address, toAdd)
	override fun revert(program: Program) = CommandHelper.removeBasicStatement(program, bodyInfoChange?.oldInfo, address)
}

@Serializable
@SerialName("RemoveBasicStatement")
data class RemoveBasicStatement(
	override val optimization: Optimization,
	override val bodyInfoChange: BodyInfoChange?,
	val address: BasicStatementAddress,
	val toRemove: BasicStatement,
	val liveVariables: Set<Variable>,
) : Command() {
	override fun apply(program: Program) = CommandHelper.removeBasicStatement(program, bodyInfoChange?.newInfo, address)

	/**
	 * Reverting would be complicated if applying lead to an empty basic block,
	 * because then the basic block does not exist any more and the id of the basic block got lost.
	 */
	override fun revert(program: Program): Program? = null
}

@Serializable
@SerialName("ReplaceBasicStatement")
data class ReplaceBasicStatement(
	override val optimization: Optimization,
	override val bodyInfoChange: BodyInfoChange?,
	val address: BasicStatementAddress,
	val old: BasicStatement,
	val replacement: BasicStatement,
	val liveVariables: Set<Variable>,
) : Command() {
	override fun apply(program: Program) = CommandHelper.replaceBasicStatement(program, bodyInfoChange?.newInfo, address, replacement)
	override fun revert(program: Program) = CommandHelper.replaceBasicStatement(program, bodyInfoChange?.oldInfo, address, old)
}

@Serializable
@SerialName("ReplaceCompoundStatement")
data class ReplaceCompoundStatement(
	override val optimization: Optimization,
	override val bodyInfoChange: BodyInfoChange?,
	val address: CompoundStatementAddress,
	val replacement: BasicBlock,
	val reason: String,
) : Command() {
	override fun apply(program: Program) = CommandHelper.replaceCompoundStatement(program, bodyInfoChange?.newInfo, address, replacement)
	override fun revert(program: Program): Program? = null
}

@Serializable
@SerialName("MappingEntry")
data class MappingEntry(val variable: Variable, val expression: Expression?)

@Serializable
sealed class ReplaceExpressionReason

@Serializable
@SerialName("CoercionReplaceExpressionReason")
data class CoercionReplaceExpressionReason(
	val old: String,
	val replacement: String,
) : ReplaceExpressionReason()

@Serializable
@SerialName("VariableReplaceExpressionReason")
data class VariableReplaceExpressionReason(
	val variable: Variable,
) : ReplaceExpressionReason()

@Serializable
@SerialName("PropagationReplaceExpressionReason")
data class PropagationReplaceExpressionReason(
	val mapping: List<MappingEntry>,
	val variable: Variable,
) : ReplaceExpressionReason()

@Serializable
@SerialName("RuleReplaceExpressionReason")
data class RuleReplaceExpressionReason(
	val old: String,
	val replacement: String,
) : ReplaceExpressionReason()

@Serializable
@SerialName("ReplaceExpression")
data class ReplaceExpression(
	override val optimization: Optimization,
	override val bodyInfoChange: BodyInfoChange?,
	val address: ExpressionAddress,
	val old: Expression,
	val replacement: Expression,
	val reason: ReplaceExpressionReason,
	val addStatement: AddBasicStatement?,
) : Command() {
	override fun apply(program: Program) = CommandHelper.replaceExpression(addStatement?.apply(program) ?: program, bodyInfoChange?.newInfo, address, replacement)
	override fun revert(program: Program) = if (addStatement == null) CommandHelper.replaceExpression(program, bodyInfoChange?.oldInfo, address, old) else null
}

@Serializable
@SerialName("TakeBranch")
data class TakeBranch(
	override val optimization: Optimization,
	override val bodyInfoChange: BodyInfoChange?,
	val address: CompoundStatementAddress,
	val condition: BasicBlock,
	val takenBranch: Compound,
	val reason: String,
) : Command() {
	override fun apply(program: Program) = CommandHelper.takeBranch(program, bodyInfoChange?.newInfo, address, condition, takenBranch)
	override fun revert(program: Program): Program? = null
}

object CommandHelper {
	fun replaceExpression(program: Program, bodyInfo: BodyInfo?, address: ExpressionAddress, replacement: Expression) =
		ProgramContext(program).mapExpression(address) { ctx ->
			ctx.statement.basicBlock.compound.body.setBodyInfoIfNonNull(bodyInfo)
			replacement
		}

	fun addBasicStatement(program: Program, bodyInfo: BodyInfo?, address: BasicStatementAddress, toAdd: BasicStatement) =
		ProgramContext(program).mapBasicStatement(address) { ctx ->
			ctx.basicBlock.compound.body.setBodyInfoIfNonNull(bodyInfo)
			ctx.basicBlock.addStatement(toAdd)
			ctx.original
		}

	fun removeBasicStatement(program: Program, bodyInfo: BodyInfo?, address: BasicStatementAddress) =
		ProgramContext(program).mapBasicStatement(address) { ctx ->
			ctx.basicBlock.compound.body.setBodyInfoIfNonNull(bodyInfo)
			null
		}

	fun replaceBasicStatement(program: Program, bodyInfo: BodyInfo?, address: BasicStatementAddress, replacement: BasicStatement) =
		ProgramContext(program).mapBasicStatement(address) { ctx ->
			ctx.basicBlock.compound.body.setBodyInfoIfNonNull(bodyInfo)
			replacement
		}

	fun replaceCompoundStatement(program: Program, bodyInfo: BodyInfo?, address: CompoundStatementAddress, replacement: BasicBlock) =
		takeBranch(program, bodyInfo, address, replacement, Compound(listOf()))

	/**
	 * Requires re-computation of Cfg
	 */
	fun takeBranch(program: Program, bodyInfo: BodyInfo?, address: CompoundStatementAddress, condition: BasicBlock, branch: Compound) =
		ProgramContext(program).mapCompound(address.compoundAddress) { ctx ->
			ctx.body.setBodyInfoIfNonNull(bodyInfo)
			val compound = ctx.original
			val statements = mutableListOf<CompoundStatement>()
			// Add statements before index.
			for (index in 0..(address.index - 2)) {
				statements.add(compound.statements[index])
			}
			ctx.useBasicBlockBuilder { basicBlockBuilder ->
				// Add statement before index.
				when (val statementBeforeIndex = compound.statements.getOrNull(address.index - 1)) {  // address.index == 0 therefore not visited
					is BasicBlock -> basicBlockBuilder.addBasicBlock(statementBeforeIndex)
					is IfStatement, is ReturnStatement, is WhileStatement -> statements.add(statementBeforeIndex)
					null -> {
					}
				}
				// Add statement at index.
				basicBlockBuilder.addBasicBlock(condition)
				// Add first branch statement.
				when (val firstBranchStatement = branch.statements.firstOrNull()) {
					is BasicBlock -> basicBlockBuilder.addBasicBlock(firstBranchStatement)
					is IfStatement -> {
						basicBlockBuilder.addBasicBlock(firstBranchStatement.expressionBlock.basicBlock)
						statements.add(
							IfStatement(
								basicBlockBuilder.toExpressionBlock(firstBranchStatement.expressionBlock.basicBlock.id, firstBranchStatement.expressionBlock.expression),
								firstBranchStatement.thenBranch,
								firstBranchStatement.elseBranch
							)
						)
					}
					is ReturnStatement -> {
						basicBlockBuilder.addBasicBlock(firstBranchStatement.expressionBlock.basicBlock)
						statements.add(
							ReturnStatement(
								basicBlockBuilder.toExpressionBlock(firstBranchStatement.expressionBlock.basicBlock.id, firstBranchStatement.expressionBlock.expression)
							)
						)
					}
					is WhileStatement -> {
						basicBlockBuilder.toBasicBlock({ firstBranchStatement.expressionBlock.basicBlock.id }, statements::add)
						statements.add(firstBranchStatement)
					}
					null -> {
					}
				}
				// Add intermediate branch statements.
				for (index in 1..(branch.statements.size - 2)) {
					statements.add(branch.statements[index])
				}
				// Add last branch statement.
				when (val lastBranchStatement = branch.statements.getOrNull(max(1, branch.statements.size - 1))) {
					is BasicBlock -> basicBlockBuilder.addBasicBlock(lastBranchStatement)
					is IfStatement, is ReturnStatement, is WhileStatement -> statements.add(lastBranchStatement)
					null -> {
					}
				}
				// Add statement after index.
				when (val statementAfterIndex = compound.statements.getOrNull(address.index + 1)) {
					is BasicBlock -> {
						basicBlockBuilder.addBasicBlock(statementAfterIndex)
						basicBlockBuilder.toBasicBlock({ statementAfterIndex.id }, statements::add)
					}
					is IfStatement -> {
						basicBlockBuilder.addBasicBlock(statementAfterIndex.expressionBlock.basicBlock)
						statements.add(
							IfStatement(
								basicBlockBuilder.toExpressionBlock(statementAfterIndex.expressionBlock.basicBlock.id, statementAfterIndex.expressionBlock.expression),
								statementAfterIndex.thenBranch,
								statementAfterIndex.elseBranch
							)
						)
					}
					is ReturnStatement -> {
						basicBlockBuilder.addBasicBlock(statementAfterIndex.expressionBlock.basicBlock)
						statements.add(
							ReturnStatement(
								basicBlockBuilder.toExpressionBlock(
									statementAfterIndex.expressionBlock.basicBlock.id, statementAfterIndex.expressionBlock.expression
								)
							)
						)
					}
					is WhileStatement -> {
						basicBlockBuilder.toBasicBlock({ statementAfterIndex.expressionBlock.basicBlock.id }, statements::add)
						statements.add(statementAfterIndex)
					}
					null -> basicBlockBuilder.toBasicBlock(null, statements::add)
				}
			}
			// Add statements after index.
			for (index in (address.index + 2)..compound.statements.lastIndex) {
				statements.add(compound.statements[index])
			}
			Compound(statements)
		}
}