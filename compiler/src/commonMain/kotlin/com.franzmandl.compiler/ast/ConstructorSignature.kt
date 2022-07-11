package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("ConstructorSignature")
data class ConstructorSignature(
	val isDefault: Boolean,
	val signature: Signature,
) : HasId {
	@Transient
	override val id = signature.toString()
}