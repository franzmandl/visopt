package com.franzmandl.compiler.common

import kotlinx.serialization.Serializable

@Serializable
enum class Phase(val prefix: String) {
	Lexer("lexical"),
	Parser("syntax"),
	TypeChecker("type"),
	;
}