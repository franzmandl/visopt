package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Body")
data class Body(
	val arguments: List<Variable>,
	val compound: Compound,
	val cfg: Cfg,
	private val bodyInfo: BodyInfo,
) {
	companion object {
		val emptyBody = Body(listOf(), Compound(listOf()), Cfg.emptyCfg, BodyInfo())
	}

	fun copyInfo() = bodyInfo.copy()
}