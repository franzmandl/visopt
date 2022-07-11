package com.franzmandl.compiler.ctx

import kotlinx.serialization.Serializable

@Serializable
data class Addressed<P>(
	val address: Address,
	val payload: P,
)