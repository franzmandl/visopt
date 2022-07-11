package com.franzmandl.compiler.ast

object BraceUtil {
	fun needsTernaryBraces(original: Expression) =
		getInnerOperand(original) is TernaryOperation

	fun needsBinaryBraces(operator: BinaryOperator, operands: BinaryOperands<Expression>, isRhs: Boolean) =
		needsBinaryBracesHelper(operator, getInnerOperand(if (isRhs) operands.rhs else operands.lhs), isRhs)

	private fun needsBinaryBracesHelper(operator: BinaryOperator, original: Expression, isRhs: Boolean) =
		original is TernaryOperation || (original is BinaryOperation && operator.order > original.operator.order) || (isRhs && original is BinaryOperation && operator.order == original.operator.order)

	fun needsUnaryBraces(original: Expression, isLogical: Boolean) =
		needsUnaryBracesHelper(getInnerOperand(original), isLogical)

	private fun needsUnaryBracesHelper(original: Expression, isLogical: Boolean) =
		original is TernaryOperation || original is BinaryOperation || (original is ArithmeticUnaryOperation) || original is IntegerLiteral || (!isLogical && original is LogicalNotUnaryOperation)

	private fun getInnerOperand(original: Expression) =
		when (original) {
			is CoercionExpression -> original.operand
			else -> original
		}
}