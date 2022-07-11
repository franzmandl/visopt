package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.Variable

data class JasminVariable(
	val id: String,
	val level: Int?,
	val type: JasminValueType,
) {
	constructor(variable: Variable) : this(variable.id, variable.level, JasminValueType.create(variable.type))
}