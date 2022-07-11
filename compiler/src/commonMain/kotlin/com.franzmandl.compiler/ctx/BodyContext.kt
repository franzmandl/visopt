package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.CfgBuilder

class BodyContext(
	private val bodyAddress: BodyAddress,
	val symbol: HasBodySymbol,
) {
	var info = symbol.body.copyInfo()
		private set
	val original = symbol.body
	private val currentCompoundAddress = CompoundAddress(bodyAddress, listOf())
	val compound get() = CompoundContext(this, { currentCompoundAddress }, original.compound)

	fun <P> address(payload: P) =
		Addressed(bodyAddress, payload)

	private fun fromCompound(compound: Compound): Body =
		Body(original.arguments, compound, CfgBuilder.build(compound), info)

	fun mapCompound(address: CompoundAddress, transform: (CompoundContext) -> Compound): Body =
		fromCompound(compound.mapCompound(address, transform))

	fun mapCompounds(transform: (CompoundContext) -> Compound): Body =
		fromCompound(transform(compound))

	fun mapCompoundStatement(address: CompoundStatementAddress, transform: (CompoundStatementContext) -> CompoundStatement): Body =
		mapCompound(address.compoundAddress) { it.mapCompoundStatement(address, transform) }

	fun mapCompoundStatements(transform: (CompoundStatementContext) -> CompoundStatement): Body =
		mapCompounds { ctx -> ctx.mapCompoundStatements(transform) }

	fun mapBasicBlock(address: BasicBlockAddress, transform: (BasicBlockContext) -> BasicBlock): Body =
		mapCompound(address.compoundStatementAddress.compoundAddress) { it.mapBasicBlock(address, transform) }

	fun mapExpressionBlock(address: ExpressionBlockAddress, transform: (ExpressionBlockContext) -> ExpressionBlock): Body =
		mapCompound(address.compoundStatementAddress.compoundAddress) { it.mapExpressionBlock(address, transform) }

	fun mapAnyBasicBlocks(transformer: AnyBlockTransformer): Body =
		mapCompounds { it.mapAnyBasicBlocks(transformer) }

	fun mapBasicStatement(address: BasicStatementAddress, transform: (BasicStatementContext) -> BasicStatement?): Body =
		mapCompound(address.basicBlockAddress.compoundStatementAddress.compoundAddress) { it.mapBasicStatement(address, transform) }

	fun mapBasicStatements(transform: (BasicStatementContext) -> BasicStatement?): Body =
		mapCompounds { it.mapBasicStatements(transform) }

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): Body =
		mapCompound(address.basicStatementAddress.basicBlockAddress.compoundStatementAddress.compoundAddress) { it.mapExpression(address, transform) }

	fun mapExpressions(transform: (ExpressionContext<Expression>) -> Expression): Body =
		mapCompounds { it.mapExpressions(transform) }

	fun visitExpressions(visitor: ExpressionVisitor<Expression>): Body =
		mapCompounds { it.visitExpressions(visitor) }

	private var oldInfo = info.copy()
	fun createBodyInfoChange() =
		if (oldInfo != info) {
			val newInfo = info.copy()
			val result = BodyInfoChange(oldInfo, newInfo)
			oldInfo = newInfo
			result
		} else {
			null
		}

	fun setBodyInfoIfNonNull(bodyInfo: BodyInfo?) {
		if (bodyInfo != null) {
			info = bodyInfo
		}
	}
}