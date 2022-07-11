package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Compound")
data class Compound(
	val statements: List<CompoundStatement>,
)