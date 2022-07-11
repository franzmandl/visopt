package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*
import kotlin.reflect.KClass
import kotlin.reflect.safeCast

class AnyPropagation<T : Expression>(
	addCommand: AddCommand,
	private val expressionClass: KClass<T>,
	optimization: Optimization,
) : AnyBlockTransformer {
	private val mapping = mutableMapOf<Variable, T?>()
	private val visitor = ExpressionReplaceVisitor(addCommand, Replacer(mapping, optimization))

	object ConstantPropagation : AnyBlockTransformer.Factory {
		override fun createAnyBlockTransformer(addCommand: AddCommand, liveOnExit: Set<Variable>) =
			AnyPropagation(addCommand, LiteralExpression::class, Optimization.ConstantPropagation)
	}

	object CopyPropagation : AnyBlockTransformer.Factory {
		override fun createAnyBlockTransformer(addCommand: AddCommand, liveOnExit: Set<Variable>) =
			AnyPropagation(addCommand, VariableAccess::class, Optimization.CopyPropagation)
	}

	private fun <R> useState(block: () -> R): R {
		val result = block()
		mapping.clear()
		return result
	}

	private fun updateMapping(variable: Variable, anyExpression: Expression): Expression {
		val expression = expressionClass.safeCast(anyExpression)
		if (expression != null && mapping[variable] != expression || expression == null && variable in mapping) {
			mapping[variable] = expression
		}
		return anyExpression
	}

	private fun visitAssignment(lhs: AssignableExpression, rhs: Expression): Assignment {
		if (lhs is VariableAccess) {
			updateMapping(lhs.variable, rhs)
		}
		return Assignment(lhs, rhs)
	}

	private fun visitBasicBlock(ctx: BasicBlockContext) =
		ctx.mapBasicStatements { ctx1 ->
			when (ctx1.original) {
				is Assignment -> visitAssignment(ctx1.original.lhs, ctx1.enterExpression(ctx1.original.rhs, 1).visitExpression(visitor))
				is ExpressionStatement -> ExpressionStatement(ctx1.enterExpression(ctx1.original.expression, 0).visitExpression(visitor))
				is VariableDeclarations, null -> ctx1.original
			}
		}

	override fun mapBasicBlock(ctx: BasicBlockContext) =
		useState { visitBasicBlock(ctx) }

	override fun mapExpressionBlock(ctx: ExpressionBlockContext) =
		useState { ctx.map({ visitBasicBlock(it) }, { it.visitExpression(visitor) }) }

	private class Replacer<T : Expression>(
		private val mapping: Map<Variable, T?>,
		private val optimization: Optimization,
	) : ExpressionReplaceVisitor.Replacer {
		override fun replaceVariableAccess(ctx: ExpressionContext<VariableAccess>) =
			mapping[ctx.original.variable]?.let { replacement ->
				if (replacement is VariableAccess && ctx.original.variable == replacement.variable) {
					null  // Do not add command for e.g. "a = a".
				} else {
					ctx.replaceExpression(
						optimization, ctx.original, replacement, PropagationReplaceExpressionReason(
							mapping.map { MappingEntry(it.key, it.value) }, ctx.original.variable
						), null
					)
				}
			}
	}
}