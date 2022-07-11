package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Member")
data class Member(
	val accessModifier: AccessModifier,
	override val id: String,
	val type: Type,
) : ClassSymbol(), HasId