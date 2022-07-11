package com.franzmandl.compiler.ctx

class AnyBasicBlockMultiTransformer(
	private val transformers: Iterable<AnyBlockTransformer>
) : AnyBlockTransformer {
	override fun mapBasicBlock(ctx: BasicBlockContext) =
		transformers.fold(ctx.original) { basicBlock, transformer ->
			transformer.mapBasicBlock(BasicBlockContext(ctx.compound, basicBlock))
		}

	override fun mapExpressionBlock(ctx: ExpressionBlockContext) =
		transformers.fold(ctx.original) { expressionBlock, transformer ->
			transformer.mapExpressionBlock(
				ExpressionBlockContext(
					BasicBlockContext(
						ctx.basicBlock.compound, expressionBlock.basicBlock
					), expressionBlock, ctx.statement
				)
			)
		}
}