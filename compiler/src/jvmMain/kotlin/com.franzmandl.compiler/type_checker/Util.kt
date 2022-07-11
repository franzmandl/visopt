package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.Location
import com.franzmandl.compiler.common.SignatureNullable
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode

object Util {
	fun createLocation(token: Token) = Location(token.line, token.charPositionInLine)
	fun createLocation(terminal: TerminalNode) = createLocation(terminal.symbol)
	fun createLocation(ctx: ParserRuleContext) = createLocation(ctx.start)

	val builtinMethods = setOf(PrintBoolMethod, PrintIntMethod, PrintStringMethod, ReadIntMethod, ReadStringMethod).associateBy { SignatureNullable(it.signature) }
}