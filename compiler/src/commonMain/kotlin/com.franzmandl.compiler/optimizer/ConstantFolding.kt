package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*
import com.franzmandl.compiler.ctx.CoercionReplaceExpressionReason as CoercionReason
import com.franzmandl.compiler.ctx.RuleReplaceExpressionReason as RuleReason

object ConstantFolding : ExpressionReplaceVisitor.Factory, ExpressionReplaceVisitor.Replacer {
	private val optimization = Optimization.ConstantFolding

	private fun replace(ctx: ExpressionContext<Expression>, old: Expression, replacement: Expression, reason: ReplaceExpressionReason, addStatement: AddBasicStatement?) =
		ctx.replaceExpression(optimization, old, replacement, reason, addStatement)

	override fun createExpressionReplaceVisitor(addCommand: AddCommand) = ExpressionReplaceVisitor(addCommand, ConstantFolding)

	override fun replaceArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>, alt: ArithmeticBinaryOperation) =
		if (operands.lhs is IntegerLiteral && operands.rhs is IntegerLiteral) {
			fun createCommand(replacement: Int) =
				replace(ctx, alt, IntegerLiteral(replacement), RuleReason("${operands.lhs.value} ${ctx.original.operator.sign} ${operands.rhs.value}", "$replacement"), null)
			when (ctx.original.operator) {
				ArithmeticBinaryOperator.Minus -> createCommand(operands.lhs.value - operands.rhs.value)
				ArithmeticBinaryOperator.Percent -> if (operands.rhs.value != 0) createCommand(operands.lhs.value % operands.rhs.value) else null
				ArithmeticBinaryOperator.Plus -> createCommand(operands.lhs.value + operands.rhs.value)
				ArithmeticBinaryOperator.ShiftLeft -> createCommand(operands.lhs.value shl operands.rhs.value)
				ArithmeticBinaryOperator.ShiftRight -> createCommand(operands.lhs.value shr operands.rhs.value)
				ArithmeticBinaryOperator.Star -> createCommand(operands.lhs.value * operands.rhs.value)
				ArithmeticBinaryOperator.Slash -> if (operands.rhs.value != 0) createCommand(operands.lhs.value / operands.rhs.value) else null
			}
		} else {
			null
		}

	override fun replaceLogicalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<LogicalBinaryOperation>, alt: LogicalBinaryOperation) =
		if (operands.lhs is BooleanLiteral && operands.rhs is BooleanLiteral) {
			fun createCommand(replacement: Boolean) =
				replace(ctx, alt, BooleanLiteral.of(replacement), RuleReason("${operands.lhs.value} ${ctx.original.operator.sign} ${operands.rhs.value}", "$replacement"), null)
			when (ctx.original.operator) {
				LogicalBinaryOperator.And -> createCommand(operands.lhs.value && operands.rhs.value)
				LogicalBinaryOperator.Or -> createCommand(operands.lhs.value || operands.rhs.value)
			}
		} else {
			null
		}

	override fun replaceRelationalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<RelationalBinaryOperation>, alt: RelationalBinaryOperation) =
		if (operands.lhs is IntegerLiteral && operands.rhs is IntegerLiteral) {
			fun createCommand(replacement: Boolean) =
				replace(ctx, alt, BooleanLiteral.of(replacement), RuleReason("${operands.lhs.value} ${ctx.original.operator.sign} ${operands.rhs.value}", "$replacement"), null)
			when (ctx.original.operator) {
				RelationalBinaryOperator.Equal -> createCommand(operands.lhs.value == operands.rhs.value)
				RelationalBinaryOperator.EqualNot -> createCommand(operands.lhs.value != operands.rhs.value)
				RelationalBinaryOperator.GreaterEqual -> createCommand(operands.lhs.value >= operands.rhs.value)
				RelationalBinaryOperator.Greater -> createCommand(operands.lhs.value > operands.rhs.value)
				RelationalBinaryOperator.SmallerEqual -> createCommand(operands.lhs.value <= operands.rhs.value)
				RelationalBinaryOperator.Smaller -> createCommand(operands.lhs.value < operands.rhs.value)
			}
		} else {
			null
		}

	override fun replaceTernaryOperation(condition: Expression, operands: BinaryOperands<Expression>, ctx: ExpressionContext<TernaryOperation>, alt: TernaryOperation) =
		when (condition) {
			BooleanLiteralFalse -> replace(ctx, alt, operands.rhs, RuleReason("false ? x : y", "y"), null)
			BooleanLiteralTrue -> replace(ctx, alt, operands.lhs, RuleReason("true ? x : y", "x"), null)
			else -> null
		}

	override fun replaceCoercionExpression(operand: Expression, ctx: ExpressionContext<CoercionExpression>, alt: CoercionExpression) =
		when (ctx.original.expectedType) {
			Type.bool -> when (operand) {
				IntegerLiteral.p0 -> replace(ctx, alt, BooleanLiteralFalse, CoercionReason("0", "false"), null)
				is IntegerLiteral -> replace(ctx, alt, BooleanLiteralTrue, CoercionReason("${operand.value}", "true"), null)
				else -> null
			}
			Type.int -> when (operand) {
				BooleanLiteralFalse -> replace(ctx, alt, IntegerLiteral.p0, CoercionReason("false", "0"), null)
				BooleanLiteralTrue -> replace(ctx, alt, IntegerLiteral.p1, CoercionReason("true", "1"), null)
				else -> null
			}
			else -> null
		}

	override fun replaceLogicalNotUnaryOperation(operand: Expression, ctx: ExpressionContext<LogicalNotUnaryOperation>, alt: LogicalNotUnaryOperation) =
		when (operand) {
			BooleanLiteralFalse -> replace(ctx, alt, BooleanLiteralTrue, RuleReason("!false", "true"), null)
			BooleanLiteralTrue -> replace(ctx, alt, BooleanLiteralFalse, RuleReason("!true", "false"), null)
			else -> null
		}

	override fun replaceArithmeticUnaryOperation(operand: Expression, ctx: ExpressionContext<ArithmeticUnaryOperation>, alt: ArithmeticUnaryOperation) =
		if (operand is IntegerLiteral) {
			when (ctx.original.operator) {
				ArithmeticUnaryOperator.Minus -> replace(ctx, alt, IntegerLiteral(-operand.value), RuleReason("-(${operand.value})", "-${operand.value}"), null)
				ArithmeticUnaryOperator.Plus -> replace(ctx, alt, operand, RuleReason("+${operand.value}", "${operand.value}"), null)
			}
		} else {
			null
		}
}