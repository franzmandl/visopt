package com.franzmandl.compiler.fpa

import com.franzmandl.compiler.ast.Cfg
import com.franzmandl.compiler.ast.Variable
import com.franzmandl.compiler.ctx.BodyContext

object FpaBuilder {
	fun build(ctx: BodyContext, liveOnExit: Iterable<Variable>): Fpa {
		val exitNode = FpaNode(Cfg.exit, setOf(), setOf())
		val map = listOf(
			FpaNode(Cfg.entry, setOf(), setOf()),
			exitNode,
		).associateByTo(mutableMapOf()) { it.id }
		ctx.mapAnyBasicBlocks(FpaAnyBlockTransformer { map[it.id] = it })
		exitNode.`in`.addAll(liveOnExit)
		val changed = ctx.original.cfg.map.keys.toMutableSet()
		changed.remove(Cfg.exit)
		var id: Int? = null
		while (changed.isNotEmpty()) {
			id = takeNextId(changed, id)
			val fpaNode = map[id]!!
			val cfgNode = ctx.original.cfg.get(id)
			cfgNode.naturalSuccessor?.let { map[it] }?.let { fpaNode.out.addAll(it.`in`) }
			cfgNode.complexSuccessor?.let { map[it] }?.let { fpaNode.out.addAll(it.`in`) }
			if (cfgNode.selfSuccessor) {
				fpaNode.out.addAll(fpaNode.`in`)
			}
			val oldInSize = fpaNode.`in`.size
			fpaNode.`in`.addAll(fpaNode.use)
			fpaNode.`in`.addAll(fpaNode.out - fpaNode.def)
			if (oldInSize != fpaNode.`in`.size) {
				cfgNode.forEachPredecessor { changed.add(it) }
			}
		}
		return map
	}

	private fun takeNextId(changed: MutableSet<Int>, current: Int?): Int {
		val nextId = current?.let { getNearestNextId(changed, it) } ?: changed.last()
		changed.remove(nextId)
		return nextId
	}

	private fun getNearestNextId(changed: Set<Int>, current: Int): Int? {
		var upperHalfMin = Cfg.exit
		var lowerHalfMax = Cfg.entry
		for (id in changed) {
			if (id in current until upperHalfMin) {
				upperHalfMin = id
			} else if (id in (lowerHalfMax + 1) until current) {
				lowerHalfMax = id
			}
		}
		return if (upperHalfMin != Cfg.exit) {
			upperHalfMin
		} else if (lowerHalfMax != Cfg.entry) {
			lowerHalfMax
		} else {
			null
		}
	}
}