package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

interface ExpressionVisitor<T> {
	fun visitArithmeticUnaryOperation(operand: T, ctx: ExpressionContext<ArithmeticUnaryOperation>): T
	fun visitLogicalNotUnaryOperation(operand: T, ctx: ExpressionContext<LogicalNotUnaryOperation>): T
	fun visitArithmeticBinaryOperation(operands: BinaryOperands<T>, ctx: ExpressionContext<ArithmeticBinaryOperation>): T
	fun visitLogicalBinaryOperation(operands: BinaryOperands<T>, ctx: ExpressionContext<LogicalBinaryOperation>): T
	fun visitRelationalBinaryOperation(operands: BinaryOperands<T>, ctx: ExpressionContext<RelationalBinaryOperation>): T
	fun visitObjectEqualsBinaryOperation(operands: BinaryOperands<T>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>): T
	fun visitTernaryOperation(condition: T, operands: BinaryOperands<T>, ctx: ExpressionContext<TernaryOperation>): T
	fun visitCoercionExpression(operand: T, ctx: ExpressionContext<CoercionExpression>): T
	fun visitBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>): T
	fun visitIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>): T
	fun visitStringLiteral(ctx: ExpressionContext<StringLiteral>): T
	fun visitNixLiteral(ctx: ExpressionContext<NixLiteral>): T
	fun visitThisExpression(ctx: ExpressionContext<ThisExpression>): T
	fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>): T
	fun visitMemberAccess(operand: T, ctx: ExpressionContext<MemberAccess>): T
	fun visitBuiltinMethodInvocation(arguments: List<T>, ctx: ExpressionContext<BuiltinMethodInvocation>): T
	fun visitMethodInvocation(operand: T, arguments: List<T>, ctx: ExpressionContext<MethodInvocation>): T
	fun visitObjectAllocation(arguments: List<T>, ctx: ExpressionContext<ObjectAllocation>): T
}