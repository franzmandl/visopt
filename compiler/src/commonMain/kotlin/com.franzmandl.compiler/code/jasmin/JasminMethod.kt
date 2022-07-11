package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.AccessModifier
import com.franzmandl.compiler.ctx.Addressed
import com.franzmandl.compiler.ctx.ProgramAddress
import kotlin.math.max

data class JasminMethod(
	val accessModifier: AccessModifier,
	val isStatic: Boolean,
	val signature: JasminSignature,
	val instructions: List<Addressed<out ChangesStack<out StackChange>>>,
) : JasminClassSymbol {
	override fun appendInstructions(appendInstruction: (Addressed<JasminInstruction>) -> Unit) {
		appendInstruction(Addressed(ProgramAddress, DotMethod(accessModifier, isStatic, signature)))
		var currentLimitStack = 0
		var limitStack = 0
		var limitLocals = 1  // 1 for this.
		val locals = mutableMapOf<String, Int>()
		for (instruction in instructions) {
			currentLimitStack = currentLimitStack - instruction.payload.stackChange.stackConsume + instruction.payload.stackChange.stackProduce
			limitStack = max(limitStack, currentLimitStack)
			if (instruction.payload is HasVariable) {
				if (instruction.payload.variable.id in locals) {
					continue
				}
				locals[instruction.payload.variable.id] = limitLocals++
			}
		}
		if (currentLimitStack != 0) {
			throw IllegalStateException("${signature}: $currentLimitStack")
		}
		appendInstruction(Addressed(ProgramAddress, DotLimitStack(limitStack)))
		appendInstruction(Addressed(ProgramAddress, DotLimitLocals(limitLocals)))
		fun getLocalIndex(variable: JasminVariable) = locals[variable.id] ?: throw IllegalStateException("${signature}: $variable")
		for (instruction in instructions) {
			appendInstruction(
				when (instruction.payload) {
					is JasminInstruction -> Addressed(instruction.address, instruction.payload)
					is Load -> Addressed(instruction.address, instruction.payload.variable.type.load(getLocalIndex(instruction.payload.variable), instruction.payload.variable.id))
					is Store -> Addressed(instruction.address, instruction.payload.variable.type.store(getLocalIndex(instruction.payload.variable), instruction.payload.variable.id))
					else -> continue
				}
			)
		}
		appendInstruction(Addressed(ProgramAddress, DotEndMethod))
	}
}