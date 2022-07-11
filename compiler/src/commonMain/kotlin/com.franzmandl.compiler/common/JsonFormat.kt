package com.franzmandl.compiler.common

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object JsonFormat {
	const val classDiscriminator = "discriminator"
	val jsonFormat = Json {
		classDiscriminator = JsonFormat.classDiscriminator
	}

	inline fun <reified T> decodeFromString(string: String) = jsonFormat.decodeFromString<T>(string)

	inline fun <reified T> encodeToString(value: T) = jsonFormat.encodeToString(value)
}