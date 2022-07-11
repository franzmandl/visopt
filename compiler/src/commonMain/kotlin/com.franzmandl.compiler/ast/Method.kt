package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Method")
data class Method(
	val methodSignature: MethodSignature,
	override val body: Body,
) : HasBodySymbol()