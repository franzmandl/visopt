package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Constructor")
data class Constructor(
	val constructorSignature: ConstructorSignature,
	override val body: Body,
) : HasBodySymbol()