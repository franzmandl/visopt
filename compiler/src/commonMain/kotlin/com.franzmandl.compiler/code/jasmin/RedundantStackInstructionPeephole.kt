package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ctx.Addressed

object RedundantStackInstructionPeephole {
	fun optimize(method: JasminMethod): JasminMethod {
		if (method.instructions.isEmpty()) {
			return method
		}
		val instructions = mutableListOf<Addressed<out ChangesStack<out StackChange>>>()
		var index = 0
		while (index < method.instructions.lastIndex) {
			val currentInstruction = method.instructions[index++]
			val nextInstruction = method.instructions[index]
			fun isLdcPop() = currentInstruction.payload is Ldc && nextInstruction.payload == Pop
			fun isLoadPop() = currentInstruction.payload is Load && nextInstruction.payload == Pop
			fun isLoadStore() = currentInstruction.payload is Load && nextInstruction.payload is Store && currentInstruction.payload.variable == nextInstruction.payload.variable
			if (isLdcPop() || isLoadPop() || isLoadStore()) {
				index++
			} else {
				instructions.add(currentInstruction)
			}
		}
		if (index < method.instructions.size) {
			instructions.add(method.instructions[index])
		}
		return JasminMethod(method.accessModifier, method.isStatic, method.signature, instructions)
	}
}