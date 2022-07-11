package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

object SideEffectVisitor : ExpressionVisitor<Boolean> {
	override fun visitArithmeticUnaryOperation(operand: Boolean, ctx: ExpressionContext<ArithmeticUnaryOperation>) = operand
	override fun visitLogicalNotUnaryOperation(operand: Boolean, ctx: ExpressionContext<LogicalNotUnaryOperation>) = operand
	override fun visitArithmeticBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<ArithmeticBinaryOperation>) =
		when (ctx.original.operator) {
			ArithmeticBinaryOperator.Minus, ArithmeticBinaryOperator.Plus, ArithmeticBinaryOperator.Star, ArithmeticBinaryOperator.ShiftLeft, ArithmeticBinaryOperator.ShiftRight ->
				operands.lhs || operands.rhs
			ArithmeticBinaryOperator.Percent, ArithmeticBinaryOperator.Slash ->
				operands.lhs || operands.rhs || ctx.original.operands.rhs == IntegerLiteral.p0 || ctx.original.operands.rhs !is IntegerLiteral  // E.g. member or variable that might be 0.
		}

	override fun visitLogicalBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<LogicalBinaryOperation>) = operands.lhs || operands.rhs
	override fun visitRelationalBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<RelationalBinaryOperation>) = operands.lhs || operands.rhs
	override fun visitObjectEqualsBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>) = operands.lhs || operands.rhs
	override fun visitTernaryOperation(condition: Boolean, operands: BinaryOperands<Boolean>, ctx: ExpressionContext<TernaryOperation>) = condition || operands.lhs || operands.rhs
	override fun visitCoercionExpression(operand: Boolean, ctx: ExpressionContext<CoercionExpression>) = operand
	override fun visitBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>) = false
	override fun visitIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>) = false
	override fun visitStringLiteral(ctx: ExpressionContext<StringLiteral>) = false
	override fun visitNixLiteral(ctx: ExpressionContext<NixLiteral>) = false
	override fun visitThisExpression(ctx: ExpressionContext<ThisExpression>) = true
	override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>) = false
	override fun visitMemberAccess(operand: Boolean, ctx: ExpressionContext<MemberAccess>) = true
	override fun visitBuiltinMethodInvocation(arguments: List<Boolean>, ctx: ExpressionContext<BuiltinMethodInvocation>) = true
	override fun visitMethodInvocation(operand: Boolean, arguments: List<Boolean>, ctx: ExpressionContext<MethodInvocation>) = true
	override fun visitObjectAllocation(arguments: List<Boolean>, ctx: ExpressionContext<ObjectAllocation>) = true
}