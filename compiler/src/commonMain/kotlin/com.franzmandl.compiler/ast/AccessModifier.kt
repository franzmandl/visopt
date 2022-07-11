package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AccessModifier {
	@SerialName("private")
	Private,

	@SerialName("public")
	Public,
	;

	val modifier = name.lowercase()

	companion object {
		val modifiers = values().associateBy { it.modifier }
	}
}