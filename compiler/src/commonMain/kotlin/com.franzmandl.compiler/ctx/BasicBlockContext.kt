package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.optimizer.Optimization

class BasicBlockContext private constructor(
	val compound: CompoundContext,
	private val statements: MutableList<BasicStatement>,
	val original: BasicBlock,
) {
	constructor(compound: CompoundContext, original: BasicBlock) : this(
		compound,
		ArrayList<BasicStatement>(original.statements.size),
		original
	)

	fun getCurrentBasicStatementAddress() =
		BasicStatementAddress(compound.getCurrentBasicBlockAddress(), statements.size)

	fun addStatement(toAdd: BasicStatement) {
		statements.add(toAdd)
	}

	fun addStatement(optimization: Optimization, toAdd: BasicStatement): AddBasicStatement {
		val address = getCurrentBasicStatementAddress()  // Fetch address BEFORE adding statement.
		statements.add(toAdd)
		return AddBasicStatement(optimization, compound.body.createBodyInfoChange(), address, toAdd)
	}

	fun addTemporaryVariable(type: Type) =
		Variable(compound.body.info.incrementTemporaryVariableId(), null, type)

	fun <P> address(payload: P) =
		Addressed(compound.getCurrentBasicBlockAddress(), payload)

	fun removeStatement(optimization: Optimization, toRemove: BasicStatement, liveVariables: Set<Variable>) =
		RemoveBasicStatement(optimization, compound.body.createBodyInfoChange(), getCurrentBasicStatementAddress(), toRemove, liveVariables)

	fun replaceStatement(optimization: Optimization, old: BasicStatement, replacement: BasicStatement, liveVariables: Set<Variable>) =
		ReplaceBasicStatement(optimization, compound.body.createBodyInfoChange(), getCurrentBasicStatementAddress(), old, replacement, liveVariables)

	fun enterBasicStatement(statement: BasicStatement?, originalIndex: Int) =
		BasicStatementContext(this, originalIndex, statement)

	fun enterAssignmentLhs(statement: Assignment, originalIndex: Int) =
		enterBasicStatement(statement, originalIndex).enterExpression(statement.lhs, 0)

	fun enterAssignmentRhs(statement: Assignment, originalIndex: Int) =
		enterBasicStatement(statement, originalIndex).enterExpression(statement.rhs, 1)

	fun enterExpressionStatementExpression(statement: ExpressionStatement, originalIndex: Int) =
		enterBasicStatement(statement, originalIndex).enterExpression(statement.expression, 0)

	fun mapBasicStatement(address: BasicStatementAddress, transform: (BasicStatementContext) -> BasicStatement?): BasicBlock =
		Util.transformGuarded { applyTransform ->
			mapBasicStatements { ctx ->
				if (ctx.originalIndex != address.index) {
					ctx.original
				} else {
					applyTransform()
					transform(ctx)
				}
			}
		}

	fun mapBasicStatements(transform: (BasicStatementContext) -> BasicStatement?): BasicBlock {
		for ((index, statement) in original.statements.withIndex()) {
			transform(enterBasicStatement(statement, index))?.let { statements.add(it) }
		}
		transform(enterBasicStatement(null, original.statements.size))?.let { statements.add(it) }
		return BasicBlock(original.id, statements)
	}

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): BasicBlock =
		mapBasicStatement(address.basicStatementAddress) { it.mapExpression(address, transform) }

	fun mapExpressions(transform: (ExpressionContext<Expression>) -> Expression): BasicBlock =
		mapBasicStatements { it.mapExpressions(transform) }

	fun visitExpressions(visitor: ExpressionVisitor<Expression>): BasicBlock =
		mapExpressions { it.visitExpression(visitor) }
}