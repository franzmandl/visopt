package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.BasicBlockBuilder
import com.franzmandl.compiler.optimizer.Optimization

class CompoundContext private constructor(
	val body: BodyContext,
	private val getParentCompoundAddress: () -> CompoundAddress,
	val statements: MutableList<CompoundStatement>,
	val original: Compound,
	private val basicBlockBuilder: BasicBlockBuilder,
) {
	constructor(body: BodyContext, getParentCompoundAddress: () -> CompoundAddress, original: Compound) : this(
		body,
		getParentCompoundAddress,
		ArrayList<CompoundStatement>(original.statements.size),
		original,
		BasicBlockBuilder(),
	)

	fun <P> address(payload: P) =
		Addressed(getCurrentCompoundAddress(), payload)

	private fun getCurrentCompoundAddress() =
		getParentCompoundAddress().let { CompoundAddress(it.bodyAddress, it.indices + listOf(statements.size)) }

	private fun getCurrentCompoundStatementAddress(offset: Int) =
		CompoundStatementAddress(getParentCompoundAddress(), statements.size + offset)

	fun getCurrentBasicBlockAddress() =
		BasicBlockAddress(getCurrentCompoundStatementAddress(0))

	fun <T> useBasicBlockBuilder(block: (BasicBlockBuilder) -> T) =
		basicBlockBuilder.use(block)

	private fun getCurrentBasicBlock(id: Int) =
		basicBlockBuilder.getCurrentBasicBlock { id }

	/**
	 * Requires re-computation of Cfg
	 */
	fun takeThenBranch(addCommand: AddCommand, optimization: Optimization, old: IfStatement, reason: String): CompoundContext {
		basicBlockBuilder.addBasicBlock(old.expressionBlock.basicBlock)
		addCommand(
			TakeBranch(
				optimization,
				body.createBodyInfoChange(),
				getCurrentCompoundStatementAddress(0),
				getCurrentBasicBlock(old.expressionBlock.basicBlock.id),
				old.thenBranch,
				reason
			)
		)
		return CompoundContext(body, getParentCompoundAddress, statements, old.thenBranch, basicBlockBuilder)
	}

	/**
	 * Requires re-computation of Cfg
	 */
	fun takeElseBranch(addCommand: AddCommand, optimization: Optimization, old: IfStatement, reason: String): CompoundContext? {
		basicBlockBuilder.addBasicBlock(old.expressionBlock.basicBlock)
		return if (old.elseBranch == null) {
			addCommand(
				ReplaceCompoundStatement(
					optimization,
					body.createBodyInfoChange(),
					getCurrentCompoundStatementAddress(0),
					getCurrentBasicBlock(old.expressionBlock.basicBlock.id),
					reason
				)
			)
			null
		} else {
			addCommand(
				TakeBranch(
					optimization,
					body.createBodyInfoChange(),
					getCurrentCompoundStatementAddress(0),
					getCurrentBasicBlock(old.expressionBlock.basicBlock.id),
					old.elseBranch,
					reason
				)
			)
			CompoundContext(body, getParentCompoundAddress, statements, old.elseBranch, basicBlockBuilder)
		}
	}

	fun removeWhileStatement(addCommand: AddCommand, optimization: Optimization, condition: BasicBlock, reason: String) {
		basicBlockBuilder.addBasicBlock(condition)
		addCommand(
			ReplaceCompoundStatement(
				optimization,
				body.createBodyInfoChange(),
				getCurrentCompoundStatementAddress(if (basicBlockBuilder.isNotEmpty()) 1 else 0),
				condition,
				reason
			)
		)
	}

	fun removeStatement(addCommand: AddCommand, optimization: Optimization, old: CompoundStatement, reason: String) {
		removeWhileStatement(addCommand, optimization, BasicBlock(old.id, listOf()), reason)
	}

	fun enterBasicBlock(basicBlock: BasicBlock) =
		BasicBlockContext(this, basicBlock)

	fun enterExpressionBlock(statement: ControlStatement) =
		ExpressionBlockContext(enterBasicBlock(statement.expressionBlock.basicBlock), statement.expressionBlock, statement)

	private fun enterBranch(compound: Compound, index: Int) =
		CompoundContext(body, { getCurrentCompoundAddress().let { CompoundAddress(it.bodyAddress, it.indices + listOf(index)) } }, compound)

	fun enterThenBranch(statement: IfStatement) =
		enterBranch(statement.thenBranch, 0)

	fun enterElseBranch(statement: IfStatement) =
		statement.elseBranch?.let { enterBranch(it, 1) }

	fun enterWhileBranch(statement: WhileStatement) =
		CompoundContext(body, ::getCurrentCompoundAddress, statement.branch)

	fun mapCompound(address: CompoundAddress, transform: (CompoundContext) -> Compound): Compound =
		if (address.head == null) {
			transform(this)
		} else {
			Util.transformGuarded { applyTransform ->
				mapCompoundStatements { ctx ->
					if (ctx.originalIndex != address.head) {
						ctx.original
					} else {
						applyTransform()
						when (ctx.original) {
							is BasicBlock -> throw IllegalStateException("Address lead to basic block.")
							is IfStatement -> {
								val tail = address.tail.tail
								when (val head = address.tail.head) {
									0 -> IfStatement(ctx.original.expressionBlock, enterThenBranch(ctx.original).mapCompound(tail, transform), ctx.original.elseBranch)
									1 -> IfStatement(
										ctx.original.expressionBlock,
										ctx.original.thenBranch,
										(enterElseBranch(ctx.original) ?: throw IllegalStateException("Else branch is null.")).mapCompound(tail, transform)
									)
									else -> throw IllegalStateException("Expected 0 or 1, got $head")
								}
							}
							is ReturnStatement -> throw IllegalStateException("Address lead to return statement.")
							is WhileStatement -> WhileStatement(ctx.original.expressionBlock, enterWhileBranch(ctx.original).mapCompound(address.tail, transform))
						}
					}
				}
			}
		}

	fun mapCompounds(transform: (CompoundContext) -> Compound): Compound {
		for (statement in original.statements) {
			statements.add(
				when (statement) {
					is BasicBlock -> statement
					is IfStatement -> IfStatement(statement.expressionBlock, transform(enterThenBranch(statement)), enterElseBranch(statement)?.let { transform(it) })
					is ReturnStatement -> statement
					is WhileStatement -> WhileStatement(statement.expressionBlock, transform(enterWhileBranch(statement)))
				}
			)
		}
		return Compound(statements)
	}

	fun mapCompoundStatement(address: CompoundStatementAddress, transform: (CompoundStatementContext) -> CompoundStatement): Compound =
		Util.transformGuarded { applyTransform ->
			mapCompoundStatements { ctx ->
				if (ctx.originalIndex != address.index) {
					ctx.original
				} else {
					applyTransform()
					transform(ctx)
				}
			}
		}

	fun mapCompoundStatements(transformStatement: (CompoundStatementContext) -> CompoundStatement): Compound {
		for ((index, statement) in original.statements.withIndex()) {
			val transformed = transformStatement(CompoundStatementContext(this, index, statement))
			if (transformed is BasicBlock && transformed.statements.isEmpty()) {
				continue
			}
			statements.add(transformed)
		}
		return Compound(statements)
	}

	fun mapBasicBlock(address: BasicBlockAddress, transform: (BasicBlockContext) -> BasicBlock): Compound =
		mapCompoundStatement(address.compoundStatementAddress) { it.mapBasicBlock(transform) }

	fun mapExpressionBlock(address: ExpressionBlockAddress, transform: (ExpressionBlockContext) -> ExpressionBlock): Compound =
		mapCompoundStatement(address.compoundStatementAddress) { it.mapExpressionBlock(transform) }

	fun mapAnyBasicBlocks(transformer: AnyBlockTransformer): Compound =
		mapCompoundStatements { it.mapAnyBasicBlocks(transformer) }

	fun mapBasicStatement(address: BasicStatementAddress, transform: (BasicStatementContext) -> BasicStatement?): Compound =
		mapBasicBlock(address.basicBlockAddress) { it.mapBasicStatement(address, transform) }

	fun mapBasicStatements(transform: (BasicStatementContext) -> BasicStatement?): Compound =
		mapCompoundStatements { it.mapBasicStatements(transform) }

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): Compound =
		mapCompoundStatement(address.basicStatementAddress.basicBlockAddress.compoundStatementAddress) { it.mapExpression(address, transform) }

	fun mapExpressions(transform: (ExpressionContext<Expression>) -> Expression): Compound =
		mapCompoundStatements { ctx ->
			when (ctx.original) {
				is BasicBlock -> enterBasicBlock(ctx.original).mapExpressions(transform)
				is IfStatement -> IfStatement(
					enterExpressionBlock(ctx.original).mapExpressions(transform),
					enterThenBranch(ctx.original).mapExpressions(transform),
					enterElseBranch(ctx.original)?.mapExpressions(transform)
				)
				is ReturnStatement -> ReturnStatement(enterExpressionBlock(ctx.original).mapExpressions(transform))
				is WhileStatement -> WhileStatement(
					enterExpressionBlock(ctx.original).mapExpressions(transform),
					enterWhileBranch(ctx.original).mapExpressions(transform)
				)
			}
		}

	fun visitExpressions(visitor: ExpressionVisitor<Expression>): Compound =
		mapExpressions { it.visitExpression(visitor) }
}