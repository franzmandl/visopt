package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*

object DeadVariableElimination {
	private val optimization = Optimization.DeadCodeElimination

	fun visitCompound(addCommand: AddCommand, ctx: CompoundContext): Compound {
		val usedVariables = mutableSetOf<Variable>()
		ctx.mapAnyBasicBlocks(BlockTransformer(VariableAccessVisitor { variable ->
			if (variable.level != null && variable !in ctx.body.original.arguments) {
				usedVariables.add(variable)
			}
		}))
		ctx.statements.clear()  // is necessary because we map twice here.
		return ctx.mapBasicStatements { ctx1 ->
			when (ctx1.original) {
				is Assignment, is ExpressionStatement, null -> ctx1.original
				is VariableDeclarations -> {
					val variables = ctx1.original.variables.filter { it in usedVariables }
					if (variables.isEmpty()) {
						addCommand(ctx1.basicBlock.removeStatement(optimization, ctx1.original, setOf()))
						null
					} else if (variables != ctx1.original.variables) {
						val replacement = VariableDeclarations(ctx1.original.type, variables)
						addCommand(ctx1.basicBlock.replaceStatement(optimization, ctx1.original, replacement, setOf()))
						replacement
					} else {
						ctx1.original
					}
				}
			}
		}
	}

	private class BlockTransformer(
		private val visitor: VariableAccessVisitor
	) : AnyBlockTransformer {
		override fun mapBasicBlock(ctx: BasicBlockContext) =
			ctx.mapBasicStatements { ctx1 ->
				when (ctx1.original) {
					is Assignment -> {
						ctx1.enterExpression(ctx1.original.lhs, 0).visitExpression(visitor)
						ctx1.enterExpression(ctx1.original.rhs, 1).visitExpression(visitor)
					}
					is ExpressionStatement ->
						ctx1.enterExpression(ctx1.original.expression, 0).visitExpression(visitor)
					is VariableDeclarations, null -> {}
				}
				ctx1.original
			}

		override fun mapExpressionBlock(ctx: ExpressionBlockContext) =
			ctx.map({ mapBasicBlock(it) }, { it.visitExpression(visitor) })
	}
}