package com.franzmandl.compiler.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LexerError")
data class LexerError(
	override val location: Location,
	override val text: String
) : PhaseMessage()