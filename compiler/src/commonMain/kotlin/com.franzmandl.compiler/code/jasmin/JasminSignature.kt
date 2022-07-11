package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.MethodSignature
import com.franzmandl.compiler.ast.Signature
import com.franzmandl.compiler.ast.Type
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("JasminSignature")
data class JasminSignature(
	val name: String,
	val argumentTypes: List<JasminValueType>,
	val returnType: JasminType,
) {
	constructor(signature: Signature, returnType: Type) : this(signature.name, mapArgumentTypes(signature.argumentTypes), JasminValueType.create(returnType))

	constructor(methodSignature: MethodSignature) : this(methodSignature.signature, methodSignature.returnType)

	companion object {
		fun createInitSignature(argumentTypes: List<JasminValueType>) = JasminSignature("<init>", argumentTypes, JasminVoid)

		fun mapArgumentTypes(argumentTypes: List<Type>) = argumentTypes.map { JasminValueType.create(it) }
	}
}