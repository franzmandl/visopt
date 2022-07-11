package com.franzmandl.compiler.common

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TypeWarning : PhaseMessage() {
	@Serializable
	@SerialName("BinaryCoercionWarning")
	data class BinaryCoercionWarning(
		override val location: Location,
		private val operator: String,
		private val foundLhs: String,
		private val foundRhs: String,
		private val resultLhs: String,
		private val resultRhs: String,
	) : TypeWarning() {
		// Operator '<OP>': Found types: '<lhs-type>', '<rhs-type>'. Coerced to: '<result-lhs-type>', '<result-rhs-type>'
		@Required
		override val text = "Operator '$operator': Found types: '$foundLhs', '$foundRhs'. Coerced to: '$resultLhs', '$resultRhs'"
	}

	@Serializable
	@SerialName("ConditionCoercionWarning")
	data class ConditionCoercionWarning(
		override val location: Location,
		private val foundType: String,
		private val resultType: String,
	) : TypeWarning() {
		// Condition: Found type: '<type>'. Coerced to: '<result-type>'
		@Required
		override val text = "Condition: Found type: '$foundType'. Coerced to: '$resultType'"
	}

	@Serializable
	@SerialName("ReturnCoercionWarning")
	data class ReturnCoercionWarning(
		override val location: Location,
		private val foundType: String,
		private val resultType: String,
	) : TypeWarning() {
		// Return: Found type: '<type>'. Coerced to: '<result-type>'
		@Required
		override val text = "Return: Found type: '$foundType'. Coerced to: '$resultType'"
	}

	@Serializable
	@SerialName("UnaryCoercionWarning")
	data class UnaryCoercionWarning(
		override val location: Location,
		private val operator: String,
		private val foundType: String,
		private val resultType: String,
	) : TypeWarning() {
		// Operator '<OP>': Found type: '<type>'. Coerced to: '<result-type>'
		@Required
		override val text = "Operator '$operator': Found type: '$foundType'. Coerced to: '$resultType'"
	}
}