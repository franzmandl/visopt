package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

class CompoundStatementContext(
	val compound: CompoundContext,
	val originalIndex: Int,
	val original: CompoundStatement,
) {
	//fun mapCompound(address: CompoundAddress, transform: (CompoundContext) -> Compound): CompoundStatement =

	fun mapCompounds(transform: (CompoundContext) -> Compound): CompoundStatement =
		when (original) {
			is BasicBlock -> original
			is IfStatement -> IfStatement(original.expressionBlock, transform(compound.enterThenBranch(original)), compound.enterElseBranch(original)?.let { transform(it) })
			is ReturnStatement -> ReturnStatement(original.expressionBlock)
			is WhileStatement -> WhileStatement(original.expressionBlock, transform(compound.enterWhileBranch(original)))
		}

	//fun mapCompoundStatement(address: CompoundStatementAddress, transform: (CompoundStatementContext) -> CompoundStatement): CompoundStatement =
	//fun mapCompoundStatements(transform: (CompoundStatementContext) -> CompoundStatement): CompoundStatement =

	fun mapBasicBlock(transform: (BasicBlockContext) -> BasicBlock): CompoundStatement =
		when (original) {
			is BasicBlock -> transform(compound.enterBasicBlock(original))
			is IfStatement -> IfStatement(compound.enterExpressionBlock(original).map(transform) { it.original }, original.thenBranch, original.elseBranch)
			is ReturnStatement -> ReturnStatement(compound.enterExpressionBlock(original).map(transform) { it.original })
			is WhileStatement -> WhileStatement(compound.enterExpressionBlock(original).map(transform) { it.original }, original.branch)
		}

	fun mapExpressionBlock(transform: (ExpressionBlockContext) -> ExpressionBlock): CompoundStatement =
		when (original) {
			is BasicBlock -> throw IllegalStateException("Address lead to BasicBlock.")
			is IfStatement -> IfStatement(transform(compound.enterExpressionBlock(original)), original.thenBranch, original.elseBranch)
			is ReturnStatement -> ReturnStatement(transform(compound.enterExpressionBlock(original)))
			is WhileStatement -> WhileStatement(transform(compound.enterExpressionBlock(original)), original.branch)
		}

	fun mapAnyBasicBlocks(transformer: AnyBlockTransformer): CompoundStatement =
		when (original) {
			is BasicBlock -> transformer.mapBasicBlock(compound.enterBasicBlock(original))
			is IfStatement -> IfStatement(
				transformer.mapExpressionBlock(compound.enterExpressionBlock(original)),
				compound.enterThenBranch(original).mapAnyBasicBlocks(transformer),
				compound.enterElseBranch(original)?.mapAnyBasicBlocks(transformer)
			)
			is ReturnStatement -> ReturnStatement(transformer.mapExpressionBlock(compound.enterExpressionBlock(original)))
			is WhileStatement -> WhileStatement(
				transformer.mapExpressionBlock(compound.enterExpressionBlock(original)),
				compound.enterWhileBranch(original).mapAnyBasicBlocks(transformer)
			)
		}

	//fun mapBasicStatement(address: BasicStatementAddress, transform: (BasicStatement?, (BasicStatement) -> Unit) -> BasicStatement?): CompoundStatement =

	fun mapBasicStatements(transform: (BasicStatementContext) -> BasicStatement?): CompoundStatement =
		mapAnyBasicBlocks(object : AnyBlockTransformer {
			override fun mapBasicBlock(ctx: BasicBlockContext): BasicBlock = ctx.mapBasicStatements(transform)
			override fun mapExpressionBlock(ctx: ExpressionBlockContext): ExpressionBlock = ctx.mapBasicStatements(transform)
		})

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): CompoundStatement =
		when (original) {
			is BasicBlock -> compound.enterBasicBlock(original).mapExpression(address, transform)
			is IfStatement -> IfStatement(compound.enterExpressionBlock(original).mapExpression(address, transform), original.thenBranch, original.elseBranch)
			is ReturnStatement -> ReturnStatement(compound.enterExpressionBlock(original).mapExpression(address, transform))
			is WhileStatement -> WhileStatement(compound.enterExpressionBlock(original).mapExpression(address, transform), original.branch)
		}

	fun mapExpressions(transform: (ExpressionContext<Expression>) -> Expression): CompoundStatement =
		when (original) {
			is BasicBlock -> compound.enterBasicBlock(original).mapExpressions(transform)
			is IfStatement -> IfStatement(
				compound.enterExpressionBlock(original).mapExpressions(transform),
				compound.enterThenBranch(original).mapExpressions(transform),
				compound.enterElseBranch(original)?.mapExpressions(transform)
			)
			is ReturnStatement -> ReturnStatement(compound.enterExpressionBlock(original).mapExpressions(transform))
			is WhileStatement -> WhileStatement(
				compound.enterExpressionBlock(original).mapExpressions(transform),
				compound.enterWhileBranch(original).mapExpressions(transform)
			)
		}

	fun visitExpressions(visitor: ExpressionVisitor<Expression>): CompoundStatement =
		mapExpressions { it.visitExpression(visitor) }
}