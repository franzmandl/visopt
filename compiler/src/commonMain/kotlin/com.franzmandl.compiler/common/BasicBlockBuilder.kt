package com.franzmandl.compiler.common

import com.franzmandl.compiler.ast.*

class BasicBlockBuilder {
	private var possibleId: Int? = null
	private val statementsBuilder = mutableListOf<BasicStatement>()
	var variables = SymbolTable<Variable>()
		private set

	fun addBasicBlock(basicBlock: BasicBlock) {
		possibleId = possibleId ?: basicBlock.id
		statementsBuilder.addAll(basicBlock.statements)
	}

	fun addStatement(statement: BasicStatement) = statementsBuilder.add(statement)

	fun getCurrentBasicBlock(getId: (() -> Int)?) =
		BasicBlock(possibleId ?: getId?.let { it() } ?: throw IllegalStateException("BasicBlock has no id."), statementsBuilder.toList())

	fun toExpressionBlock(id: Int, expression: Expression): ExpressionBlock {
		val result = ExpressionBlock(getCurrentBasicBlock { id }, expression)
		possibleId = null
		statementsBuilder.clear()
		variables = SymbolTable()
		return result
	}

	fun toExpressionBlock(expressionBlock: ExpressionBlock): ExpressionBlock {
		addBasicBlock(expressionBlock.basicBlock)
		return toExpressionBlock(expressionBlock.basicBlock.id, expressionBlock.expression)
	}

	fun toBasicBlock(getId: (() -> Int)?, addCompoundStatement: (CompoundStatement) -> Unit) {
		if (statementsBuilder.isNotEmpty()) {
			addCompoundStatement(getCurrentBasicBlock(getId))
			possibleId = null
			statementsBuilder.clear()
			variables = SymbolTable()
		}
	}

	fun isNotEmpty() = statementsBuilder.isNotEmpty()

	fun <T> use(block: (BasicBlockBuilder) -> T): T {
		val result = block(this)
		if (statementsBuilder.isNotEmpty()) {
			throw IllegalStateException("BasicBlockBuilder contains statements.")
		}
		if (variables.isNotEmpty()) {
			throw IllegalStateException("BasicBlockBuilder contains variables.")
		}
		return result
	}
}