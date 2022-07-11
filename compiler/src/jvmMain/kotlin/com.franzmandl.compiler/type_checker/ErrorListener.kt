package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.common.LexerError
import com.franzmandl.compiler.common.Location
import com.franzmandl.compiler.common.ParserError
import com.franzmandl.compiler.generated.JovaLexer
import com.franzmandl.compiler.generated.JovaParser
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

object ErrorListener {
	fun setLexerErrorListener(lexer: JovaLexer, addLexerError: (LexerError) -> Unit) {
		lexer.removeErrorListeners()
		lexer.addErrorListener(object : BaseErrorListener() {
			override fun syntaxError(
				recognizer: Recognizer<*, *>?,
				offendingSymbol: Any?,
				line: Int,
				position: Int,
				message: String,
				e: RecognitionException?
			) {
				addLexerError(LexerError(Location(line, position), message))
			}
		})
	}

	fun setParserErrorListener(parser: JovaParser, addParserError: (ParserError) -> Unit) {
		parser.removeErrorListeners()
		parser.addErrorListener(object : BaseErrorListener() {
			override fun syntaxError(
				recognizer: Recognizer<*, *>?,
				offendingSymbol: Any?,
				line: Int,
				position: Int,
				message: String,
				e: RecognitionException?
			) {
				addParserError(ParserError(Location(line, position), message))
			}
		})
	}
}