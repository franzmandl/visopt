package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

class BasicStatementContext(
	val basicBlock: BasicBlockContext,
	val originalIndex: Int,
	val original: BasicStatement?,
) {
	fun <P> address(payload: P) =
		Addressed(basicBlock.getCurrentBasicStatementAddress(), payload)

	fun enterExpression(expression: Expression, rootIndex: Int): ExpressionContext<Expression> =
		ExpressionContext(rootIndex, listOf(), expression, this)

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): BasicStatement =
		when (original) {
			is Assignment -> if (address.rootIndex == 1) {
				Assignment(original.lhs, enterExpression(original.rhs, address.rootIndex).mapExpression(address, transform))
			} else {
				throw IllegalStateException("Expected 1, got ${address.rootIndex}")
			}
			is ExpressionStatement -> if (address.rootIndex == 0) {
				ExpressionStatement(enterExpression(original.expression, address.rootIndex).mapExpression(address, transform))
			} else {
				throw IllegalStateException("Expected 0, got ${address.rootIndex}")
			}
			is VariableDeclarations -> throw IllegalStateException("Address lead to Declaration.")
			null -> throw IllegalStateException("Statement is null.")
		}

	fun mapExpressions(transform: (ExpressionContext<Expression>) -> Expression): BasicStatement? =
		when (original) {
			is Assignment -> Assignment(original.lhs, transform(enterExpression(original.rhs, 1)))
			is ExpressionStatement -> ExpressionStatement(transform(enterExpression(original.expression, 0)))
			is VariableDeclarations, null -> original
		}

	fun visitExpressions(visitor: ExpressionVisitor<Expression>): BasicStatement? =
		mapExpressions { it.visitExpression(visitor) }
}