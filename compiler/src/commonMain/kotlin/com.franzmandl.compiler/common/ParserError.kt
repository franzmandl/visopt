package com.franzmandl.compiler.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ParserError")
data class ParserError(
	override val location: Location,
	override val text: String,
) : PhaseMessage()