package com.franzmandl.compiler.ctx

object Util {
	fun <T> transformGuarded(transform: (() -> Unit) -> T): T {
		var transformed = false
		val result = transform {
			if (transformed) {
				throw IllegalStateException("Already transformed.")
			} else {
				transformed = true
			}
		}
		if (!transformed) {
			throw IllegalStateException("No transformation was applied.")
		}
		return result
	}
}