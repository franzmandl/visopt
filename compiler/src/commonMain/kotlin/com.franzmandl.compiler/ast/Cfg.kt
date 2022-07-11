package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("Cfg")
data class Cfg(
	val list: List<CfgNode>,
) {
	@Transient
	val map = list.associateBy { it.id }

	fun get(id: Int) = map[id] ?: throw IllegalStateException("Node with ID '$id' not found.")

	companion object {
		const val entry = -1
		const val exit = Int.MAX_VALUE
		val emptyCfg = Cfg(
			listOf(
				CfgNode(entry, null, setOf(), exit, null, selfSuccessor = false, inverted = false),
				CfgNode(exit, entry, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		)
	}
}

@Serializable
@SerialName("CfgNode")
data class CfgNode(
	val id: Int,
	val naturalPredecessor: Int?,
	val complexPredecessors: Set<Int>,
	val naturalSuccessor: Int?,
	val complexSuccessor: Int?,
	val selfSuccessor: Boolean,
	val inverted: Boolean,
) {
	fun hasComplexPredecessors() = complexPredecessors.isNotEmpty()

	fun forEachPredecessor(block: (Int) -> Unit) {
		naturalPredecessor?.let(block)
		complexPredecessors.forEach(block)
	}
}