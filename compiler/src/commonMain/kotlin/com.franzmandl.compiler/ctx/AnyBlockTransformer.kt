package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.BasicBlock
import com.franzmandl.compiler.ast.ExpressionBlock
import com.franzmandl.compiler.ast.Variable

interface AnyBlockTransformer {
	fun mapBasicBlock(ctx: BasicBlockContext): BasicBlock
	fun mapExpressionBlock(ctx: ExpressionBlockContext): ExpressionBlock

	interface Factory {
		fun createAnyBlockTransformer(addCommand: AddCommand, liveOnExit: Set<Variable>): AnyBlockTransformer
	}
}