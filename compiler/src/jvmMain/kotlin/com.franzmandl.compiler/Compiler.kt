package com.franzmandl.compiler

import com.franzmandl.compiler.lexer.LexerPhase
import com.franzmandl.compiler.optimizer.OptimizerPhase
import org.antlr.v4.runtime.CharStreams

object Compiler {
	fun fromFileName(fileName: String) = LexerPhase(fileName, CharStreams.fromFileName(fileName))

	fun fromJsonString(jsonString: String) = OptimizerPhase.fromJsonString(jsonString)

	fun fromString(fileName: String, input: String) = LexerPhase(fileName, CharStreams.fromString(input))
}