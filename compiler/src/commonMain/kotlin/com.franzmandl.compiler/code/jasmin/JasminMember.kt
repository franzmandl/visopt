package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.Member
import com.franzmandl.compiler.ctx.Addressed
import com.franzmandl.compiler.ctx.ProgramAddress

data class JasminMember(
	val member: Member,
	val isStatic: Boolean,
) : JasminClassSymbol {
	override fun appendInstructions(appendInstruction: (Addressed<JasminInstruction>) -> Unit) {
		appendInstruction(Addressed(ProgramAddress, DotField(member, isStatic)))
	}
}