package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.Program
import com.franzmandl.compiler.ast.Variable
import com.franzmandl.compiler.code.jasmin.ClassBuilder
import com.franzmandl.compiler.code.jasmin.JasminOptimizerPhase
import com.franzmandl.compiler.code.jova.JovaFormatter
import com.franzmandl.compiler.common.JsonFormat
import com.franzmandl.compiler.common.LoopMode
import com.franzmandl.compiler.common.PhaseMessage
import com.franzmandl.compiler.ctx.AddCommand
import com.franzmandl.compiler.ctx.Address
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("OptimizerPhase")
data class OptimizerPhase(
	var program: Program,
	val typeWarnings: List<PhaseMessage>,
) {
	fun optimize(addCommand: AddCommand, mode: LoopMode, optimizations: Set<Optimization>, address: Address, liveOnExit: Set<Variable>): OptimizerPhase {
		program = Optimizer.optimize(program, addCommand, mode, optimizations, address, liveOnExit)
		return this
	}

	fun generateJasmin() = JasminOptimizerPhase(program.classes.map { ClassBuilder.build(it, program.fileName, program.needsScanner) })

	fun formatJova(address: Address) = JovaFormatter.format(program, address)

	fun toJsonString() = JsonFormat.encodeToString(this)

	companion object {
		fun fromJsonString(jsonString: String) = JsonFormat.decodeFromString<OptimizerPhase>(jsonString)
	}
}