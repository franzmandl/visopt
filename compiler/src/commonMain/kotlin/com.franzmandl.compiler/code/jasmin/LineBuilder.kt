package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.AccessModifier

class LineBuilder(prefix: String) {
	private val builder = StringBuilder(prefix)

	fun argument() = raw(" ")

	fun argument(accessModifier: AccessModifier) = argument(accessModifier.modifier)

	fun argument(label: Label) = argument(label.name)

	fun argument(value: Int) = argument().raw(value)

	fun argument(value: String) = argument().raw(value)

	fun argumentStatic(isStatic: Boolean) =
		when {
			isStatic -> argument("static")
			else -> this
		}

	private fun classSymbol(classId: String) = raw(classId).raw("/")

	fun comment(comment: String) = raw("  ; ").raw(comment)

	fun memberId(id: String, type: JasminType) = raw(id).argument().type(type)

	fun member(classId: String, id: String, type: JasminType) = classSymbol(classId).memberId(id, type)

	fun methodId(signature: JasminSignature) = raw(signature.name).types(signature.argumentTypes).type(signature.returnType)

	fun method(classId: String, signature: JasminSignature) = classSymbol(classId).methodId(signature)

	private fun raw(value: Int): LineBuilder {
		builder.append(value)
		return this
	}

	private fun raw(value: String): LineBuilder {
		builder.append(value)
		return this
	}

	private fun type(argumentType: JasminType) = raw(argumentType.id)

	fun types(argumentTypes: List<JasminType>): LineBuilder {
		raw("(")
		for (argumentType in argumentTypes) {
			type(argumentType)
		}
		raw(")")
		return this
	}

	fun variable(index: Int) = raw(index)

	fun build() = builder.toString()
}