package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ctx.Addressed

sealed interface JasminClassSymbol {
	fun appendInstructions(appendInstruction: (Addressed<JasminInstruction>) -> Unit)
}