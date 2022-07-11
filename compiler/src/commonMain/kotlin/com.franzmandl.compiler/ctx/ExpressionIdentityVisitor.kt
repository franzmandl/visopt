package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

interface ExpressionIdentityVisitor : ExpressionVisitor<Expression> {
	override fun visitArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>): Expression = ctx.original
	override fun visitLogicalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<LogicalBinaryOperation>): Expression = ctx.original
	override fun visitRelationalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<RelationalBinaryOperation>): Expression = ctx.original
	override fun visitObjectEqualsBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>): Expression = ctx.original
	override fun visitTernaryOperation(condition: Expression, operands: BinaryOperands<Expression>, ctx: ExpressionContext<TernaryOperation>): Expression = ctx.original
	override fun visitCoercionExpression(operand: Expression, ctx: ExpressionContext<CoercionExpression>): Expression = ctx.original
	override fun visitBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>): Expression = ctx.original
	override fun visitIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>): Expression = ctx.original
	override fun visitStringLiteral(ctx: ExpressionContext<StringLiteral>): Expression = ctx.original
	override fun visitNixLiteral(ctx: ExpressionContext<NixLiteral>): Expression = ctx.original
	override fun visitThisExpression(ctx: ExpressionContext<ThisExpression>): Expression = ctx.original
	override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>): Expression = ctx.original
	override fun visitMemberAccess(operand: Expression, ctx: ExpressionContext<MemberAccess>): Expression = ctx.original
	override fun visitBuiltinMethodInvocation(arguments: List<Expression>, ctx: ExpressionContext<BuiltinMethodInvocation>): Expression = ctx.original
	override fun visitMethodInvocation(operand: Expression, arguments: List<Expression>, ctx: ExpressionContext<MethodInvocation>): Expression = ctx.original
	override fun visitObjectAllocation(arguments: List<Expression>, ctx: ExpressionContext<ObjectAllocation>): Expression = ctx.original
	override fun visitLogicalNotUnaryOperation(operand: Expression, ctx: ExpressionContext<LogicalNotUnaryOperation>): Expression = ctx.original
	override fun visitArithmeticUnaryOperation(operand: Expression, ctx: ExpressionContext<ArithmeticUnaryOperation>): Expression = ctx.original
}