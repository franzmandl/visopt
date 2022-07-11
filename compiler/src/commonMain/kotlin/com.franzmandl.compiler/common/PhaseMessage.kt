package com.franzmandl.compiler.common

import kotlinx.serialization.Serializable

@Serializable
sealed class PhaseMessage {
	abstract val location: Location
	abstract val text: String
}