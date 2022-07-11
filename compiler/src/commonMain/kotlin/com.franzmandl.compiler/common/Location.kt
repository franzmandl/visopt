package com.franzmandl.compiler.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Location")
data class Location(
	val line: Int,
	private val position: Int,
) {
	override fun toString() = "$line:${position + 1}"
}