package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.AddCommand
import com.franzmandl.compiler.ctx.ExpressionContext
import com.franzmandl.compiler.ctx.ExpressionReplaceVisitor
import com.franzmandl.compiler.ctx.VariableReplaceExpressionReason

object ThreeAddressCode : ExpressionReplaceVisitor.Factory, ExpressionReplaceVisitor.Replacer {
	private val optimization = Optimization.ThreeAddressCode

	override fun createExpressionReplaceVisitor(addCommand: AddCommand) = ExpressionReplaceVisitor(addCommand, ThreeAddressCode)

	private fun replace(ctx: ExpressionContext<Expression>, old: Expression, expression: Expression) =
		if (ctx.isRootExpression()) {
			null
		} else {
			val variable = ctx.statement.basicBlock.addTemporaryVariable(expression.type ?: Type.langObject)
			val lhs = VariableAccess(variable)
			ctx.replaceExpression(optimization, old, lhs, VariableReplaceExpressionReason(variable), ctx.statement.basicBlock.addStatement(optimization, Assignment(lhs, expression)))
		}

	override fun replaceArithmeticUnaryOperation(operand: Expression, ctx: ExpressionContext<ArithmeticUnaryOperation>, alt: ArithmeticUnaryOperation) =
		replace(ctx, alt, ArithmeticUnaryOperation(ctx.original.operator, operand))

	override fun replaceLogicalNotUnaryOperation(operand: Expression, ctx: ExpressionContext<LogicalNotUnaryOperation>, alt: LogicalNotUnaryOperation) =
		replace(ctx, alt, LogicalNotUnaryOperation(ctx.original.id, operand))

	override fun replaceArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>, alt: ArithmeticBinaryOperation) =
		replace(ctx, alt, ArithmeticBinaryOperation(ctx.original.operator, operands))

	override fun replaceLogicalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<LogicalBinaryOperation>, alt: LogicalBinaryOperation) =
		replace(ctx, alt, LogicalBinaryOperation(ctx.original.id, ctx.original.operator, operands))

	override fun replaceRelationalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<RelationalBinaryOperation>, alt: RelationalBinaryOperation) =
		replace(ctx, alt, RelationalBinaryOperation(ctx.original.id, ctx.original.operator, operands))

	override fun replaceObjectEqualsBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>, alt: ObjectEqualsBinaryOperation) =
		replace(ctx, alt, ObjectEqualsBinaryOperation(ctx.original.id, ctx.original.operator, operands))

	override fun replaceTernaryOperation(condition: Expression, operands: BinaryOperands<Expression>, ctx: ExpressionContext<TernaryOperation>, alt: TernaryOperation) =
		replace(ctx, alt, TernaryOperation(ctx.original.id, condition, operands))

	override fun replaceCoercionExpression(operand: Expression, ctx: ExpressionContext<CoercionExpression>, alt: CoercionExpression) =
		replace(ctx, alt, CoercionExpression(ctx.original.id, operand, ctx.original.expectedType, ctx.original.acceptedType))

	override fun replaceBuiltinMethodInvocation(arguments: List<Expression>, ctx: ExpressionContext<BuiltinMethodInvocation>, alt: BuiltinMethodInvocation) =
		replace(ctx, alt, BuiltinMethodInvocation(ctx.original.method, arguments))

	override fun replaceMethodInvocation(operand: Expression, arguments: List<Expression>, ctx: ExpressionContext<MethodInvocation>, alt: MethodInvocation) =
		replace(ctx, alt, MethodInvocation(operand, ctx.original.clazz, ctx.original.methodSignature, arguments))

	override fun replaceObjectAllocation(arguments: List<Expression>, ctx: ExpressionContext<ObjectAllocation>, alt: ObjectAllocation) =
		replace(ctx, alt, ObjectAllocation(ctx.original.type, ctx.original.constructorSignature, arguments))
}