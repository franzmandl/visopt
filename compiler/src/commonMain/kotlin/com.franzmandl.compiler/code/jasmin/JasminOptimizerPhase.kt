package com.franzmandl.compiler.code.jasmin

class JasminOptimizerPhase(
	val classes: List<JasminClass>,
) {
	fun optimizeRedundantStackInstructionPeephole() = JasminOptimizerPhase(classes.map { clazz ->
		JasminClass(clazz.sourceFileName, clazz.id, clazz.symbols.map { classSymbol ->
			when (classSymbol) {
				is JasminMember -> classSymbol
				is JasminMethod -> RedundantStackInstructionPeephole.optimize(classSymbol)
			}
		})
	})
}