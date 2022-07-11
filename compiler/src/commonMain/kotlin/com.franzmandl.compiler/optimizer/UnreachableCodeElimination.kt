package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.BasicBlockBuilder
import com.franzmandl.compiler.ctx.AddCommand
import com.franzmandl.compiler.ctx.CompoundContext

object UnreachableCodeElimination {
	private val optimization = Optimization.DeadCodeElimination

	fun visitCompound(addCommand: AddCommand, ctx: CompoundContext) =
		ctx.useBasicBlockBuilder { basicBlockBuilder ->
			visitCompoundHelper(addCommand, ctx, basicBlockBuilder)
			basicBlockBuilder.toBasicBlock(null, ctx.statements::add)
			Compound(ctx.statements)
		}

	private fun visitCompoundHelper(addCommand: AddCommand, ctx: CompoundContext, basicBlockBuilder: BasicBlockBuilder) {
		var removeStatement = false
		for (statement in ctx.original.statements) {
			if (removeStatement) {
				ctx.removeStatement(addCommand, optimization, statement, "return")
			} else {
				when (statement) {
					is BasicBlock -> basicBlockBuilder.addBasicBlock(statement)
					is IfStatement -> {
						when (statement.expressionBlock.expression) {
							BooleanLiteralTrue -> {
								visitCompoundHelper(addCommand, ctx.takeThenBranch(addCommand, optimization, statement, "true"), basicBlockBuilder)
							}
							BooleanLiteralFalse -> {
								ctx.takeElseBranch(addCommand, optimization, statement, "false")?.let { visitCompoundHelper(addCommand, it, basicBlockBuilder) }
							}
							else -> IfStatement(
								basicBlockBuilder.toExpressionBlock(statement.expressionBlock),
								visitCompound(addCommand, ctx.enterThenBranch(statement)),
								ctx.enterElseBranch(statement)?.let { visitCompound(addCommand, it) },
							).apply { ctx.statements.add(this) }
						}
					}
					is ReturnStatement -> {
						ReturnStatement(basicBlockBuilder.toExpressionBlock(statement.expressionBlock)).apply { ctx.statements.add(this) }
						removeStatement = true
					}
					is WhileStatement -> {
						if (statement.expressionBlock.expression == BooleanLiteralFalse) {
							ctx.removeWhileStatement(addCommand, optimization, statement.expressionBlock.basicBlock, "false")
						} else {
							basicBlockBuilder.toBasicBlock(null, ctx.statements::add)
							WhileStatement(statement.expressionBlock, visitCompound(addCommand, ctx.enterWhileBranch(statement))).apply { ctx.statements.add(this) }
						}
					}
				}
			}
		}
	}
}