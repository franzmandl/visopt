package com.franzmandl.compiler.ast

import kotlinx.serialization.Serializable

@Serializable
sealed class ClassSymbol

@Serializable
sealed class HasBodySymbol : ClassSymbol() {
	abstract val body: Body
}