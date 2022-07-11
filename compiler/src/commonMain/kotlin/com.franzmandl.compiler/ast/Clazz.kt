package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Clazz")
data class Clazz(
	val id: String,
	val symbols: List<ClassSymbol>,
) {
	companion object {
		const val mainId = "Main"
	}
}