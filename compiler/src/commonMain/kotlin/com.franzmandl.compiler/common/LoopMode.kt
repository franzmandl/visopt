package com.franzmandl.compiler.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("LoopMode")
data class LoopMode(val endInclusive: Int, val step: Int) {
	companion object {
		val infinite = LoopMode(1, 0)
		val once = LoopMode(0, 1)
	}

	fun loopWhile(action: () -> Boolean) {
		var counter = 0
		while (counter <= endInclusive && action()) {
			counter += step
		}
	}
}