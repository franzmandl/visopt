package com.franzmandl.compiler.reflection

interface BaseClass {
	val name: String
	fun append(appendString: (String) -> Unit)
	fun appendDiscriminator(appendString: (String) -> Unit)
}