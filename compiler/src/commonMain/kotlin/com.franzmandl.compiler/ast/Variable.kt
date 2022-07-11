package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Variable")
data class Variable(
	override val id: String,
	val level: Int?,
	val type: Type,
) : HasId