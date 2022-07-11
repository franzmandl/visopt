package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ctx.Addressed

class InstructionFormatter(
	var appendString: (String) -> Unit,
) {
	var appendAddress = false
	private var previousDotInstruction: DotInstruction? = null

	fun appendInstruction(instruction: Addressed<JasminInstruction>) {
		when (instruction.payload) {
			is DotInstruction -> appendDotInstruction(instruction.payload)
			is Label -> {
				appendString("\n")
				appendString(instruction.payload.line.build())
			}
			else -> {
				appendString("\n  ")
				appendString(instruction.payload.line.build())
			}
		}
		if (appendAddress) {
			appendString("  ; " + instruction.address)
		}
	}

	private fun appendDotInstruction(dotInstruction: DotInstruction) {
		if (previousDotInstruction != null) {
			appendString("\n")
		}
		val startFieldSection = dotInstruction is DotField && previousDotInstruction !is DotField
		if (startFieldSection || dotInstruction is DotMethod) {
			appendString("\n")
		}
		previousDotInstruction = dotInstruction
		appendString(dotInstruction.line.build())
	}
}