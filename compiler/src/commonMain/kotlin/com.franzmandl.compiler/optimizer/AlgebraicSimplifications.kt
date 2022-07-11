package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.AddCommand
import com.franzmandl.compiler.ctx.ExpressionContext
import com.franzmandl.compiler.ctx.ExpressionReplaceVisitor
import com.franzmandl.compiler.ctx.RuleReplaceExpressionReason as RuleReason

object AlgebraicSimplifications : ExpressionReplaceVisitor.Factory, ExpressionReplaceVisitor.Replacer {
	private fun replace(ctx: ExpressionContext<Expression>, old: Expression, replacement: Expression, rule: RuleReason) =
		ctx.replaceExpression(Optimization.AlgebraicSimplifications, old, replacement, rule, null)

	override fun createExpressionReplaceVisitor(addCommand: AddCommand) = ExpressionReplaceVisitor(addCommand, AlgebraicSimplifications)

	override fun replaceArithmeticUnaryOperation(operand: Expression, ctx: ExpressionContext<ArithmeticUnaryOperation>, alt: ArithmeticUnaryOperation) =
		when (ctx.original.operator) {
			ArithmeticUnaryOperator.Plus -> replace(ctx, alt, operand, RuleReason("+x", "x"))
			ArithmeticUnaryOperator.Minus -> when {
				operand is ArithmeticUnaryOperation && operand.operator == ArithmeticUnaryOperator.Minus -> replace(ctx, alt, operand.operand, RuleReason("-(-x)", "x"))
				else -> null
			}
		}

	override fun replaceLogicalNotUnaryOperation(operand: Expression, ctx: ExpressionContext<LogicalNotUnaryOperation>, alt: LogicalNotUnaryOperation) =
		if (operand is LogicalNotUnaryOperation) {
			replace(ctx, alt, operand.operand, RuleReason("!!x", "x"))
		} else {
			null
		}

	override fun replaceArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>, alt: ArithmeticBinaryOperation) =
		when (ctx.original.operator) {
			ArithmeticBinaryOperator.Plus ->
				when {
					operands.lhs == IntegerLiteral.p0 -> replace(ctx, alt, operands.rhs, RuleReason("0 + x", "x"))
					operands.rhs == IntegerLiteral.p0 -> replace(ctx, alt, operands.lhs, RuleReason("x + 0", "x"))
					operands.rhs is ArithmeticUnaryOperation && operands.rhs.operator == ArithmeticUnaryOperator.Minus ->
						replace(
							ctx,
							alt,
							ArithmeticBinaryOperation(ArithmeticBinaryOperator.Minus, BinaryOperands(operands.lhs, operands.rhs.operand)), RuleReason("x + -y", "x - y")
						)
					else -> null
				}
			ArithmeticBinaryOperator.Minus ->
				when {
					operands.lhs == IntegerLiteral.p0 -> replace(ctx, alt, ArithmeticUnaryOperation(ArithmeticUnaryOperator.Minus, operands.rhs), RuleReason("0 - x", "-x"))
					operands.rhs == IntegerLiteral.p0 -> replace(ctx, alt, operands.lhs, RuleReason("x - 0", "x"))
					operands.rhs is ArithmeticUnaryOperation && operands.rhs.operator == ArithmeticUnaryOperator.Minus ->
						replace(
							ctx,
							alt,
							ArithmeticBinaryOperation(ArithmeticBinaryOperator.Plus, BinaryOperands(operands.lhs, operands.rhs.operand)), RuleReason("x - -y", "x + y")
						)
					else -> null
				}
			ArithmeticBinaryOperator.Star ->
				when {
					operands.lhs == IntegerLiteral.p0 -> replace(ctx, alt, IntegerLiteral.p0, RuleReason("0 * x", "0"))
					operands.rhs == IntegerLiteral.p0 -> replace(ctx, alt, IntegerLiteral.p0, RuleReason("x * 0", "0"))
					operands.lhs == IntegerLiteral.p1 -> replace(ctx, alt, operands.rhs, RuleReason("1 * x", "x"))
					operands.rhs == IntegerLiteral.p1 -> replace(ctx, alt, operands.lhs, RuleReason("x * 1", "x"))
					operands.lhs == IntegerLiteral.m1 -> replace(ctx, alt, ArithmeticUnaryOperation(ArithmeticUnaryOperator.Minus, operands.rhs), RuleReason("-1 * x", "-x"))
					operands.rhs == IntegerLiteral.m1 -> replace(ctx, alt, ArithmeticUnaryOperation(ArithmeticUnaryOperator.Minus, operands.lhs), RuleReason("x * -1", "-x"))
					else -> null
				}
			ArithmeticBinaryOperator.Slash ->
				when {
					operands.lhs == IntegerLiteral.p0 && operands.rhs != IntegerLiteral.p0 -> replace(ctx, alt, IntegerLiteral.p0, RuleReason("0 / x", "0"))
					operands.rhs == IntegerLiteral.p1 -> replace(ctx, alt, operands.lhs, RuleReason("x / 1", "x"))
					operands.rhs == IntegerLiteral.m1 -> replace(ctx, alt, ArithmeticUnaryOperation(ArithmeticUnaryOperator.Minus, operands.lhs), RuleReason("x / -1", "-x"))
					else -> null
				}
			ArithmeticBinaryOperator.Percent ->
				when {
					operands.lhs == IntegerLiteral.p0 && operands.rhs != IntegerLiteral.p0 -> replace(ctx, alt, IntegerLiteral.p0, RuleReason("0 % x", "0"))
					operands.rhs == IntegerLiteral.p1 -> replace(ctx, alt, IntegerLiteral.p0, RuleReason("x % 1", "0"))
					operands.rhs == IntegerLiteral.m1 -> replace(ctx, alt, IntegerLiteral.p0, RuleReason("x % -1", "0"))
					else -> null
				}
			else -> null
		}

	override fun replaceLogicalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<LogicalBinaryOperation>, alt: LogicalBinaryOperation) =
		when (ctx.original.operator) {
			LogicalBinaryOperator.And ->
				when {
					operands.lhs == BooleanLiteralFalse -> replace(ctx, alt, BooleanLiteralFalse, RuleReason("false && x", "false"))
					operands.rhs == BooleanLiteralFalse -> replace(ctx, alt, BooleanLiteralFalse, RuleReason("x && false", "false"))
					operands.lhs == BooleanLiteralTrue -> replace(ctx, alt, operands.rhs, RuleReason("true && x", "x"))
					operands.rhs == BooleanLiteralTrue -> replace(ctx, alt, operands.lhs, RuleReason("x && true", "x"))
					else -> null
				}
			LogicalBinaryOperator.Or ->
				when {
					operands.lhs == BooleanLiteralTrue -> replace(ctx, alt, BooleanLiteralTrue, RuleReason("true || x", "true"))
					operands.rhs == BooleanLiteralTrue -> replace(ctx, alt, BooleanLiteralTrue, RuleReason("x || true", "true"))
					operands.lhs == BooleanLiteralFalse -> replace(ctx, alt, operands.rhs, RuleReason("false || x", "x"))
					operands.rhs == BooleanLiteralFalse -> replace(ctx, alt, operands.lhs, RuleReason("x || false", "x"))
					else -> null
				}
		}

	override fun replaceTernaryOperation(condition: Expression, operands: BinaryOperands<Expression>, ctx: ExpressionContext<TernaryOperation>, alt: TernaryOperation) =
		when {
			operands.rhs == BooleanLiteralFalse -> replace(
				ctx,
				alt,
				LogicalBinaryOperation(
					ctx.statement.basicBlock.compound.body.info.incrementLogical(LogicalBinaryOperator.And),
					LogicalBinaryOperator.And,
					BinaryOperands(condition, operands.lhs)
				),
				RuleReason("x ? y : false", "x && y")
			)
			operands.lhs == BooleanLiteralTrue -> replace(
				ctx,
				alt,
				LogicalBinaryOperation(
					ctx.statement.basicBlock.compound.body.info.incrementLogical(LogicalBinaryOperator.Or),
					LogicalBinaryOperator.Or,
					BinaryOperands(condition, operands.rhs)
				),
				RuleReason("x ? true : y", "x || y")
			)
			else -> null
		}
}