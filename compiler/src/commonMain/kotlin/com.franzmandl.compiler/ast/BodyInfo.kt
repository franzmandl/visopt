package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max

@Serializable
@SerialName("BodyInfo")
data class BodyInfo(
	private var coercionToBoolCounter: Int = 1,
	private var coercionToAnyCounter: Int = 1,
	private var basicBlockCounter: Int = 1,
	private var logicalAndCounter: Int = 1,
	private var logicalOrCounter: Int = 1,
	private var logicalNotCounter: Int = 1,
	private var relationalCounter: Int = 1,
	private var objectEqualsCounter: Int = 1,
	private var ternaryCounter: Int = 1,
	private var temporaryVariableCounter: Int = 1,
) {
	fun incrementCoercion(type: Type) = when (type) {
		Type.bool -> coercionToBoolCounter++
		else -> coercionToAnyCounter++
	}

	fun incrementBasicBlock() = basicBlockCounter++

	fun incrementLogical(operator: LogicalBinaryOperator) = when (operator) {
		LogicalBinaryOperator.And -> logicalAndCounter++
		LogicalBinaryOperator.Or -> logicalOrCounter++
	}

	fun incrementLogicalNot() = logicalNotCounter++

	fun incrementRelational() = relationalCounter++

	fun incrementObjectEquals() = objectEqualsCounter++

	fun incrementTernary() = ternaryCounter++

	fun incrementTemporaryVariableId() = TemporaryVariableHelper.formatId(temporaryVariableCounter++)
	fun updateTemporaryVariableCounter(id: String) =
		TemporaryVariableHelper.parseNumber(id)?.let {
			temporaryVariableCounter = max(temporaryVariableCounter, it + 1)
			true
		} ?: false

	private object TemporaryVariableHelper {
		private const val prefix = "tmp"
		private val regex = Regex("^$prefix([1-9][0-9]*)\$")

		fun formatId(number: Int) = "$prefix$number"

		fun parseNumber(id: String) =
			regex.matchEntire(id)?.groups?.get(1)?.value?.toInt()
	}
}