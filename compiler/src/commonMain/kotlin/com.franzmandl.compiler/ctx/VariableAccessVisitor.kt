package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.Variable
import com.franzmandl.compiler.ast.VariableAccess

class VariableAccessVisitor(
	private val onVariableAccess: (Variable) -> Unit,
) : ExpressionIdentityVisitor {
	override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>): VariableAccess {
		onVariableAccess(ctx.original.variable)
		return ctx.original
	}
}