package com.franzmandl.compiler.parser

import com.franzmandl.compiler.common.LexerError
import com.franzmandl.compiler.common.ParserError
import com.franzmandl.compiler.common.Phase
import com.franzmandl.compiler.generated.JovaLexer
import com.franzmandl.compiler.generated.JovaParser
import com.franzmandl.compiler.misc.PhaseMessages
import com.franzmandl.compiler.type_checker.ErrorListener
import com.franzmandl.compiler.type_checker.TypeCheckerPhase
import org.antlr.v4.runtime.CommonTokenStream

class ParserPhase(
	private val fileName: String,
	private val lexer: JovaLexer,
	private val lexerErrors: PhaseMessages<LexerError>,
) {
	fun getAllTokens(): PhaseMessages<LexerError> {
		lexer.allTokens
		return lexerErrors
	}

	fun parser(): TypeCheckerPhase {
		val parserErrors = PhaseMessages<ParserError>(Phase.Parser, lexerErrors, false)
		val parser = JovaParser(CommonTokenStream(lexer))
		ErrorListener.setParserErrorListener(parser, parserErrors::add)
		return TypeCheckerPhase(fileName, parser.program(), parserErrors)
	}
}