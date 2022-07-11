package com.franzmandl.compiler.fpa

import com.franzmandl.compiler.ast.Variable

class FpaNode(
	val id: Int,
	val def: Set<Variable>,
	val use: Set<Variable>,
) {
	val `in` = mutableSetOf<Variable>()
	val out = mutableSetOf<Variable>()
}