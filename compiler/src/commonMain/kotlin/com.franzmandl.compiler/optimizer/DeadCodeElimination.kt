package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*
import com.franzmandl.compiler.fpa.FpaBuilder

class DeadCodeElimination(
	private val addCommand: AddCommand,
	private val liveOnExit: Set<Variable>,
) : AnyBlockTransformer {
	private val optimization = Optimization.DeadCodeElimination
	private val liveness = mutableListOf<MutableSet<Variable>>()

	companion object : AnyBlockTransformer.Factory {
		override fun createAnyBlockTransformer(addCommand: AddCommand, liveOnExit: Set<Variable>) =
			DeadCodeElimination(addCommand, liveOnExit)
	}

	private fun <R> useState(block: () -> R): R {
		val result = block()
		liveness.clear()
		return result
	}

	private fun visitBasicBlock(ctx: BasicBlockContext, expressionCtx: ExpressionContext<Expression>?): BasicBlock {
		ctx.original.statements.mapTo(liveness) { mutableSetOf() }
		val fpa = FpaBuilder.build(ctx.compound.body, liveOnExit)
		val fpaNode = fpa[ctx.original.id]!!
		liveness.add(fpaNode.out)
		expressionCtx?.visitExpression(LivenessVisitor { liveness.last().add(it) })
		// Backward pass: gather liveness information.
		for (index in ctx.original.statements.indices.reversed()) {
			liveness[index].addAll(liveness[index + 1])
			val visitor = LivenessVisitor { liveness[index].add(it) }
			when (val statement = ctx.original.statements[index]) {
				is Assignment -> {
					if (statement.lhs is VariableAccess) {
						liveness[index].remove(statement.lhs.variable)
					} else {
						ctx.enterAssignmentLhs(statement, index).visitExpression(visitor)
					}
					ctx.enterAssignmentRhs(statement, index).visitExpression(visitor)
				}
				is ExpressionStatement -> ctx.enterExpressionStatementExpression(statement, index).visitExpression(visitor)
				is VariableDeclarations -> {}
			}
		}
		// Forward pass: eliminate dead statements.
		return ctx.mapBasicStatements { ctx1 ->
			val livenessIndex = ctx1.originalIndex + 1
			val liveVariables = if (livenessIndex < liveness.size) liveness[livenessIndex] else setOf()
			fun isVariableLive(variable: Variable) = variable in liveVariables
			when (ctx1.original) {
				is Assignment ->
					if (ctx1.original.lhs is VariableAccess) {
						when {
							isVariableLive(ctx1.original.lhs.variable) -> ctx1.original
							ctx.enterAssignmentRhs(ctx1.original, ctx1.originalIndex).visitExpression(SideEffectVisitor) -> {
								val replacement = ExpressionStatement(ctx1.original.rhs)
								addCommand(ctx.replaceStatement(optimization, ctx1.original, replacement, liveVariables))
								replacement
							}
							else -> {
								addCommand(ctx.removeStatement(optimization, ctx1.original, liveVariables))
								null
							}
						}
					} else {
						ctx1.original
					}
				is ExpressionStatement ->
					if (ctx.enterExpressionStatementExpression(ctx1.original, ctx1.originalIndex).visitExpression(SideEffectVisitor)) {
						ctx1.original
					} else {
						addCommand(ctx.removeStatement(optimization, ctx1.original, liveVariables))
						null
					}
				is VariableDeclarations, null -> ctx1.original
			}
		}
	}

	override fun mapBasicBlock(ctx: BasicBlockContext) =
		useState { visitBasicBlock(ctx, null) }

	override fun mapExpressionBlock(ctx: ExpressionBlockContext) =
		useState { ctx.map({ visitBasicBlock(it, ctx.expression) }, { it.original }) }

	private class LivenessVisitor(
		private val setVariableLive: (Variable) -> Unit,
	) : ExpressionIdentityVisitor {
		override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>): VariableAccess {
			setVariableLive(ctx.original.variable)
			return ctx.original
		}
	}
}