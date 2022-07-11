package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.AddCommand
import com.franzmandl.compiler.ctx.ExpressionContext
import com.franzmandl.compiler.ctx.ExpressionReplaceVisitor
import com.franzmandl.compiler.ctx.RuleReplaceExpressionReason as RuleReason

object ReductionInStrength : ExpressionReplaceVisitor.Factory, ExpressionReplaceVisitor.Replacer {
	private val powerOfTwos: Map<IntegerLiteral, IntegerLiteral>

	init {
		var result = 1
		powerOfTwos = (1..30).associate {
			result *= 2
			IntegerLiteral(result) to IntegerLiteral(it)
		}
	}

	private fun replace(ctx: ExpressionContext<Expression>, old: Expression, replacement: Expression, rule: RuleReason) =
		ctx.replaceExpression(Optimization.ReductionInStrength, old, replacement, rule, null)

	override fun createExpressionReplaceVisitor(addCommand: AddCommand) = ExpressionReplaceVisitor(addCommand, ReductionInStrength)

	private fun isSimpleMultiplication(operand1: Expression, operand2: Expression) = operand1 == IntegerLiteral.p2 && (operand2 is LiteralExpression || operand2 is VariableAccess)

	override fun replaceArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>, alt: ArithmeticBinaryOperation) =
		when (ctx.original.operator) {
			ArithmeticBinaryOperator.Star ->
				when {
					isSimpleMultiplication(operands.lhs, operands.rhs) ->
						replace(ctx, alt, ArithmeticBinaryOperation(ArithmeticBinaryOperator.Plus, BinaryOperands(operands.rhs, operands.rhs)), RuleReason("2 * x", "x + x"))
					isSimpleMultiplication(operands.rhs, operands.lhs) ->
						replace(ctx, alt, ArithmeticBinaryOperation(ArithmeticBinaryOperator.Plus, BinaryOperands(operands.lhs, operands.lhs)), RuleReason("x * 2", "x + x"))
					else ->
						powerOfTwos[operands.lhs]?.let {
							replace(ctx, alt, ArithmeticBinaryOperation(ArithmeticBinaryOperator.ShiftLeft, BinaryOperands(operands.rhs, it)), RuleReason("2^i * x", "x << i"))
						}
							?: powerOfTwos[operands.rhs]?.let {
								replace(ctx, alt, ArithmeticBinaryOperation(ArithmeticBinaryOperator.ShiftLeft, BinaryOperands(operands.lhs, it)), RuleReason("x * 2^i", "x << i"))
							}
				}
			ArithmeticBinaryOperator.Slash ->
				powerOfTwos[operands.rhs]?.let {
					replace(ctx, alt, ArithmeticBinaryOperation(ArithmeticBinaryOperator.ShiftRight, BinaryOperands(operands.lhs, it)), RuleReason("x / 2^i", "x >> i"))
				}
			else -> null
		}
}