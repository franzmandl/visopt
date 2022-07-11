package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*

class ProgramContext(
	private val original: Program
) {
	fun mapBody(address: BodyAddress, transform: (BodyContext) -> Body): Program =
		Program(original.fileName, original.needsScanner, Util.transformGuarded { applyTransform ->
			original.classes.map { clazz ->
				if (clazz.id != address.classId) {
					clazz
				} else {
					Clazz(clazz.id, clazz.symbols.map { symbol ->
						when (symbol) {
							is Constructor -> if (symbol.constructorSignature.signature != address.signature) {
								symbol
							} else {
								applyTransform()
								Constructor(symbol.constructorSignature, transform(BodyContext(address, symbol)))
							}
							is Member -> symbol
							is Method -> if (symbol.methodSignature.signature != address.signature) {
								symbol
							} else {
								applyTransform()
								Method(symbol.methodSignature, transform(BodyContext(address, symbol)))
							}
						}
					})
				}
			}
		})

	fun mapBodies(transform: (BodyContext) -> Body): Program =
		Program(original.fileName, original.needsScanner, original.classes.map { clazz ->
			Clazz(clazz.id, clazz.symbols.map { symbol ->
				when (symbol) {
					is Constructor -> Constructor(symbol.constructorSignature, transform(BodyContext(BodyAddress(clazz.id, symbol.constructorSignature.signature), symbol)))
					is Member -> symbol
					is Method -> Method(symbol.methodSignature, transform(BodyContext(BodyAddress(clazz.id, symbol.methodSignature.signature), symbol)))
				}
			})
		})

	fun mapCompound(address: CompoundAddress, transform: (CompoundContext) -> Compound): Program =
		mapBody(address.bodyAddress) { it.mapCompound(address, transform) }

	fun mapCompounds(transform: (CompoundContext) -> Compound): Program =
		mapBodies { it.mapCompounds(transform) }

	fun mapCompoundStatement(address: CompoundStatementAddress, transform: (CompoundStatementContext) -> CompoundStatement): Program =
		mapBody(address.compoundAddress.bodyAddress) { it.mapCompoundStatement(address, transform) }

	fun mapCompoundStatements(transform: (CompoundStatementContext) -> CompoundStatement): Program =
		mapBodies { it.mapCompoundStatements(transform) }

	fun mapBasicBlock(address: BasicBlockAddress, transform: (BasicBlockContext) -> BasicBlock): Program =
		mapBody(address.compoundStatementAddress.compoundAddress.bodyAddress) { it.mapBasicBlock(address, transform) }

	fun mapExpressionBlock(address: ExpressionBlockAddress, transform: (ExpressionBlockContext) -> ExpressionBlock): Program =
		mapBody(address.compoundStatementAddress.compoundAddress.bodyAddress) { it.mapExpressionBlock(address, transform) }

	fun mapAnyBasicBlocks(transformer: AnyBlockTransformer): Program =
		mapBodies { it.mapAnyBasicBlocks(transformer) }

	fun mapBasicStatement(address: BasicStatementAddress, transform: (BasicStatementContext) -> BasicStatement?): Program =
		mapBody(address.basicBlockAddress.compoundStatementAddress.compoundAddress.bodyAddress) { it.mapBasicStatement(address, transform) }

	fun mapBasicStatements(transform: (BasicStatementContext) -> BasicStatement?): Program =
		mapBodies { it.mapBasicStatements(transform) }

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): Program =
		mapBody(address.basicStatementAddress.basicBlockAddress.compoundStatementAddress.compoundAddress.bodyAddress) { it.mapExpression(address, transform) }

	fun mapExpressions(transform: (ExpressionContext<Expression>) -> Expression): Program =
		mapBodies { it.mapExpressions(transform) }

	fun visitExpressions(visitor: ExpressionVisitor<Expression>): Program =
		mapBodies { it.visitExpressions(visitor) }
}