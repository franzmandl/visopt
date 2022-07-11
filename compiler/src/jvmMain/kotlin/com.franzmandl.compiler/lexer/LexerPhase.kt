package com.franzmandl.compiler.lexer

import com.franzmandl.compiler.common.LexerError
import com.franzmandl.compiler.common.Phase
import com.franzmandl.compiler.generated.JovaLexer
import com.franzmandl.compiler.misc.PhaseMessages
import com.franzmandl.compiler.parser.ParserPhase
import com.franzmandl.compiler.type_checker.ErrorListener
import org.antlr.v4.runtime.CharStream

class LexerPhase(
	private val fileName: String,
	private val input: CharStream,
) {
	fun checkTypes() = parser().checkTypes()

	fun getAllTokens() = lexer().getAllTokens()

	fun lexer(): ParserPhase {
		val lexerErrors = PhaseMessages<LexerError>(Phase.Lexer, null, false)
		val lexer = JovaLexer(input)
		ErrorListener.setLexerErrorListener(lexer, lexerErrors::add)
		return ParserPhase(fileName, lexer, lexerErrors)
	}

	fun parser() = lexer().parser()
}