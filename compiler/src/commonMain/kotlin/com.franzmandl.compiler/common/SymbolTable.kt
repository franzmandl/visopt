package com.franzmandl.compiler.common

import com.franzmandl.compiler.ast.HasId

class SymbolTable<Symbol : HasId> : Iterable<Symbol> {
	private val map: MutableMap<String, Symbol> = LinkedHashMap()

	/**
	 * @return True if was added, false if already there.
	 */
	fun add(symbol: Symbol) = map.put(symbol.id, symbol) == null

	operator fun contains(id: String) = id in map

	operator fun get(id: String) = map[id]

	override fun iterator() = map.values.iterator()

	fun isNotEmpty() = map.isNotEmpty()
}