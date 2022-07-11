package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Signature")
data class Signature(
	val name: String,
	val argumentTypes: List<Type>,
) {
	override fun toString() = name + "(" + argumentTypes.joinToString(" ") + ")"
}