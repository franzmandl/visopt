package com.franzmandl.compiler.ctx

class AnyBlockTransformerExpressionReplaceVisitorProxy(
	val visitor: ExpressionReplaceVisitor
) : AnyBlockTransformer {
	override fun mapBasicBlock(ctx: BasicBlockContext) =
		ctx.visitExpressions(visitor)

	override fun mapExpressionBlock(ctx: ExpressionBlockContext) =
		ctx.visitExpressions(visitor)
}