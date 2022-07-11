package com.franzmandl.compiler.misc

import com.franzmandl.compiler.ast.Program
import com.franzmandl.compiler.common.TypeError
import com.franzmandl.compiler.common.TypeWarning
import com.franzmandl.compiler.ctx.Address
import com.franzmandl.compiler.optimizer.OptimizerPhase

class SerializationPhase(
	val program: Program,
	val typeErrors: PhaseMessages<TypeError>,
	val typeWarnings: PhaseMessages<TypeWarning>,
) {
	fun formatJova(address: Address) = optimize().formatJova(address)

	fun generateJasmin() = optimize().generateJasmin()

	fun optimize(): OptimizerPhase {
		typeErrors.check()
		return OptimizerPhase(program, typeWarnings.messages)
	}

	fun toJsonString() = optimize().toJsonString()
}