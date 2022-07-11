package com.franzmandl.compiler.common

import com.franzmandl.compiler.ast.Signature
import com.franzmandl.compiler.ast.Type
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SignatureNullable")
data class SignatureNullable(
	val name: String,
	val argumentTypes: List<Type?>,
) {
	constructor(signature: Signature) : this(signature.name, signature.argumentTypes)

	override fun toString() = name + "(" + argumentTypes.joinToString(" ") { Type.toString(it) } + ")"
}