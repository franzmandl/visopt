package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.Type
import com.franzmandl.compiler.ctx.Addressed
import com.franzmandl.compiler.ctx.ProgramAddress

data class JasminClass(
	val sourceFileName: String,
	val id: String,
	val symbols: List<JasminClassSymbol>,
) {
	val fileName = "$id.j"

	fun appendInstructions(appendInstruction: (Addressed<JasminInstruction>) -> Unit) {
		appendInstruction(Addressed(ProgramAddress, DotSource(sourceFileName)))
		appendInstruction(Addressed(ProgramAddress, DotClass(id)))
		appendInstruction(Addressed(ProgramAddress, DotSuper(Type.langObject.id)))
		for (symbol in symbols) {
			symbol.appendInstructions(appendInstruction)
		}
	}

	fun appendStrings(appendString: (String) -> Unit) {
		appendInstructions(InstructionFormatter(appendString)::appendInstruction)
		appendString("\n")
	}
}