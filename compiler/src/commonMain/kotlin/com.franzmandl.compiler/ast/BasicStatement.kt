package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class BasicStatement

@Serializable
@SerialName("Assignment")
data class Assignment(
	val lhs: AssignableExpression,
	val rhs: Expression,
) : BasicStatement()

@Serializable
@SerialName("ExpressionStatement")
data class ExpressionStatement(
	val expression: Expression,
) : BasicStatement()

@Serializable
@SerialName("VariableDeclarations")
data class VariableDeclarations(
	val type: Type,
	val variables: List<Variable>,
) : BasicStatement()