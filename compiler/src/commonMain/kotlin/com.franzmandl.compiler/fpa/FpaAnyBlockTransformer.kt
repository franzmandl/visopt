package com.franzmandl.compiler.fpa

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*

class FpaAnyBlockTransformer(
	private val addNode: (FpaNode) -> Unit,
) : AnyBlockTransformer {
	override fun mapBasicBlock(ctx: BasicBlockContext): BasicBlock {
		addNode(createNode(ctx, null))
		return ctx.original
	}

	override fun mapExpressionBlock(ctx: ExpressionBlockContext): ExpressionBlock {
		addNode(createNode(ctx.basicBlock, ctx.expression))
		return ctx.original
	}

	private fun createNode(ctx: BasicBlockContext, expressionCtx: ExpressionContext<Expression>?): FpaNode {
		val def = mutableSetOf<Variable>()
		val use = mutableSetOf<Variable>()
		val readVariableVisitor = VariableAccessVisitor { variable ->
			if (variable !in def) {
				use.add(variable)
			}
		}
		ctx.mapBasicStatements { ctx1 ->
			ctx1.visitExpressions(readVariableVisitor)
			when(ctx1.original) {
				is Assignment -> {
					if (ctx1.original.lhs is MemberAccess) {
						ctx.enterAssignmentLhs(ctx1.original, ctx1.originalIndex).visitExpression(readVariableVisitor)  // Treat innermost variable access as read. Ignores write to this.
					} else if (ctx1.original.lhs is VariableAccess) {
						def.add(ctx1.original.lhs.variable)
					}
				}
				is VariableDeclarations, is ExpressionStatement, null -> {}
			}
			ctx1.original
		}
		expressionCtx?.visitExpression(readVariableVisitor)
		return FpaNode(ctx.original.id, def, use)
	}
}