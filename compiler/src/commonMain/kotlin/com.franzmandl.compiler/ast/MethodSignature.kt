package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("MethodSignature")
data class MethodSignature(
	val accessModifier: AccessModifier,
	val isMain: Boolean,
	val signature: Signature,
	val returnType: Type,
) : HasId {
	@Transient
	override val id = signature.toString()
}