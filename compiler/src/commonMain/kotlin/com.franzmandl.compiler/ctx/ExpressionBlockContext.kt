package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

class ExpressionBlockContext(
	val basicBlock: BasicBlockContext,
	val original: ExpressionBlock,
	val statement: ControlStatement,
) {
	val expression = basicBlock.enterBasicStatement(null, basicBlock.original.statements.size).enterExpression(original.expression, 0)

	fun map(transformBasicBlock: (BasicBlockContext) -> BasicBlock, transformExpression: (ExpressionContext<Expression>) -> Expression): ExpressionBlock =
		ExpressionBlock(transformBasicBlock(basicBlock), transformExpression(expression))

	fun mapBasicStatement(address: BasicStatementAddress, transform: (BasicStatementContext) -> BasicStatement?): ExpressionBlock =
		map({ it.mapBasicStatement(address, transform) }, { it.original })

	fun mapBasicStatements(transform: (BasicStatementContext) -> BasicStatement?): ExpressionBlock =
		map({ it.mapBasicStatements(transform) }, { it.original })

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): ExpressionBlock =
		if (address.basicStatementAddress.index == basicBlock.original.statements.size && address.rootIndex == 0) {
			map({ it.original }, { it.mapExpression(address, transform) })
		} else {
			map({ it.mapExpression(address, transform) }, { it.original })
		}

	fun mapExpressions(transform: (ExpressionContext<Expression>) -> Expression): ExpressionBlock =
		map({ it.mapExpressions(transform) }, { transform(it) })

	fun visitExpressions(visitor: ExpressionVisitor<Expression>): ExpressionBlock =
		mapExpressions { it.visitExpression(visitor) }
}