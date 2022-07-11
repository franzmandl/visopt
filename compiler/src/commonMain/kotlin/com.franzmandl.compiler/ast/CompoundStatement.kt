package com.franzmandl.compiler.ast

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class CompoundStatement {
	abstract val id: Int
}

@Serializable
@SerialName("BasicBlock")
data class BasicBlock(
	override val id: Int,
	val statements: List<BasicStatement>,
) : CompoundStatement()

@Serializable
@SerialName("ExpressionBlock")
data class ExpressionBlock(
	val basicBlock: BasicBlock,
	val expression: Expression,
)

@Serializable
sealed class ControlStatement : CompoundStatement() {
	abstract val expressionBlock: ExpressionBlock
}

@Serializable
@SerialName("IfStatement")
data class IfStatement(
	override val expressionBlock: ExpressionBlock,
	val thenBranch: Compound,
	val elseBranch: Compound?,
) : ControlStatement() {
	@Required
	override val id = expressionBlock.basicBlock.id
}

@Serializable
@SerialName("ReturnStatement")
data class ReturnStatement(
	override val expressionBlock: ExpressionBlock,
) : ControlStatement() {
	@Required
	override val id = expressionBlock.basicBlock.id
}

@Serializable
@SerialName("WhileStatement")
data class WhileStatement(
	override val expressionBlock: ExpressionBlock,
	val branch: Compound,
) : ControlStatement() {
	@Required
	override val id = expressionBlock.basicBlock.id
}