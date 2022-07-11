package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

class ExpressionReplaceVisitor(
	private val addCommand: AddCommand,
	private val replacer: Replacer,
) : ExpressionVisitor<Expression> {
	interface Factory {
		fun createExpressionReplaceVisitor(addCommand: AddCommand): ExpressionReplaceVisitor
	}

	interface Replacer {
		fun replaceArithmeticUnaryOperation(operand: Expression, ctx: ExpressionContext<ArithmeticUnaryOperation>, alt: ArithmeticUnaryOperation): ReplaceExpression? = null
		fun replaceLogicalNotUnaryOperation(operand: Expression, ctx: ExpressionContext<LogicalNotUnaryOperation>, alt: LogicalNotUnaryOperation): ReplaceExpression? = null
		fun replaceArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>, alt: ArithmeticBinaryOperation): ReplaceExpression? = null
		fun replaceLogicalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<LogicalBinaryOperation>, alt: LogicalBinaryOperation): ReplaceExpression? = null
		fun replaceRelationalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<RelationalBinaryOperation>, alt: RelationalBinaryOperation): ReplaceExpression? = null
		fun replaceObjectEqualsBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>, alt: ObjectEqualsBinaryOperation): ReplaceExpression? = null
		fun replaceTernaryOperation(condition: Expression, operands: BinaryOperands<Expression>, ctx: ExpressionContext<TernaryOperation>, alt: TernaryOperation): ReplaceExpression? = null
		fun replaceCoercionExpression(operand: Expression, ctx: ExpressionContext<CoercionExpression>, alt: CoercionExpression): ReplaceExpression? = null
		fun replaceBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>): ReplaceExpression? = null
		fun replaceIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>): ReplaceExpression? = null
		fun replaceStringLiteral(ctx: ExpressionContext<StringLiteral>): ReplaceExpression? = null
		fun replaceNixLiteral(ctx: ExpressionContext<NixLiteral>): ReplaceExpression? = null
		fun replaceThisExpression(ctx: ExpressionContext<ThisExpression>): ReplaceExpression? = null
		fun replaceVariableAccess(ctx: ExpressionContext<VariableAccess>): ReplaceExpression? = null
		fun replaceMemberAccess(operand: Expression, ctx: ExpressionContext<MemberAccess>, alt: MemberAccess): ReplaceExpression? = null
		fun replaceBuiltinMethodInvocation(arguments: List<Expression>, ctx: ExpressionContext<BuiltinMethodInvocation>, alt: BuiltinMethodInvocation): ReplaceExpression? = null
		fun replaceMethodInvocation(operand: Expression, arguments: List<Expression>, ctx: ExpressionContext<MethodInvocation>, alt: MethodInvocation): ReplaceExpression? = null
		fun replaceObjectAllocation(arguments: List<Expression>, ctx: ExpressionContext<ObjectAllocation>, alt: ObjectAllocation): ReplaceExpression? = null
	}

	private fun <E : Expression> replace(command: ReplaceExpression?, alt: E): Expression =
		if (command != null) {
			addCommand(command)
			command.replacement
		} else {
			alt
		}

	override fun visitArithmeticUnaryOperation(operand: Expression, ctx: ExpressionContext<ArithmeticUnaryOperation>): Expression =
		ArithmeticUnaryOperation(ctx.original.operator, operand).let { alt -> replace(replacer.replaceArithmeticUnaryOperation(operand, ctx, alt), alt) }

	override fun visitLogicalNotUnaryOperation(operand: Expression, ctx: ExpressionContext<LogicalNotUnaryOperation>): Expression =
		LogicalNotUnaryOperation(ctx.original.id, operand).let { alt -> replace(replacer.replaceLogicalNotUnaryOperation(operand, ctx, alt), alt) }

	override fun visitArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>): Expression =
		ArithmeticBinaryOperation(ctx.original.operator, operands).let { alt -> replace(replacer.replaceArithmeticBinaryOperation(operands, ctx, alt), alt) }

	override fun visitLogicalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<LogicalBinaryOperation>): Expression =
		LogicalBinaryOperation(ctx.original.id, ctx.original.operator, operands).let { alt -> replace(replacer.replaceLogicalBinaryOperation(operands, ctx, alt), alt) }

	override fun visitRelationalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<RelationalBinaryOperation>): Expression =
		RelationalBinaryOperation(ctx.original.id, ctx.original.operator, operands).let { alt -> replace(replacer.replaceRelationalBinaryOperation(operands, ctx, alt), alt) }

	override fun visitObjectEqualsBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>): Expression =
		ObjectEqualsBinaryOperation(ctx.original.id, ctx.original.operator, operands).let { alt -> replace(replacer.replaceObjectEqualsBinaryOperation(operands, ctx, alt), alt) }

	override fun visitTernaryOperation(condition: Expression, operands: BinaryOperands<Expression>, ctx: ExpressionContext<TernaryOperation>): Expression =
		TernaryOperation(ctx.original.id, condition, operands).let { alt -> replace(replacer.replaceTernaryOperation(condition, operands, ctx, alt), alt) }

	override fun visitCoercionExpression(operand: Expression, ctx: ExpressionContext<CoercionExpression>): Expression =
		CoercionExpression(ctx.original.id, operand, ctx.original.expectedType, ctx.original.acceptedType).let { alt -> replace(replacer.replaceCoercionExpression(operand, ctx, alt), alt) }

	override fun visitBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>): Expression = replace(replacer.replaceBooleanLiteral(ctx), ctx.original)
	override fun visitIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>): Expression = replace(replacer.replaceIntegerLiteral(ctx), ctx.original)
	override fun visitStringLiteral(ctx: ExpressionContext<StringLiteral>): Expression = replace(replacer.replaceStringLiteral(ctx), ctx.original)
	override fun visitNixLiteral(ctx: ExpressionContext<NixLiteral>): Expression = replace(replacer.replaceNixLiteral(ctx), ctx.original)
	override fun visitThisExpression(ctx: ExpressionContext<ThisExpression>): Expression = replace(replacer.replaceThisExpression(ctx), ctx.original)
	override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>): Expression = replace(replacer.replaceVariableAccess(ctx), ctx.original)

	override fun visitMemberAccess(operand: Expression, ctx: ExpressionContext<MemberAccess>): Expression =
		MemberAccess(operand, ctx.original.clazz, ctx.original.member).let { alt -> replace(replacer.replaceMemberAccess(operand, ctx, alt), alt) }

	override fun visitBuiltinMethodInvocation(arguments: List<Expression>, ctx: ExpressionContext<BuiltinMethodInvocation>): Expression =
		BuiltinMethodInvocation(ctx.original.method, arguments).let { alt -> replace(replacer.replaceBuiltinMethodInvocation(arguments, ctx, alt), alt) }

	override fun visitMethodInvocation(operand: Expression, arguments: List<Expression>, ctx: ExpressionContext<MethodInvocation>): Expression =
		MethodInvocation(operand, ctx.original.clazz, ctx.original.methodSignature, arguments).let { alt -> replace(replacer.replaceMethodInvocation(operand, arguments, ctx, alt), alt) }

	override fun visitObjectAllocation(arguments: List<Expression>, ctx: ExpressionContext<ObjectAllocation>): Expression =
		ObjectAllocation(ctx.original.type, ctx.original.constructorSignature, arguments).let { alt -> replace(replacer.replaceObjectAllocation(arguments, ctx, alt), alt) }
}