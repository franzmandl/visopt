package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Program")
data class Program(
	val fileName: String,
	val needsScanner: Boolean,
	val classes: List<Clazz>,
)