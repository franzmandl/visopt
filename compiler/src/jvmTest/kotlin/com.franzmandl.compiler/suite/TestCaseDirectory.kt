package com.franzmandl.compiler.suite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("TestCaseDirectory")
data class TestCaseDirectory(
	val invertExcluded: Boolean,
	val excludedPrefixes: Map<String, String>,
	val subDirectoryName: String,
)