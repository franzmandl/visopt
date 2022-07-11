package com.franzmandl.compiler.reflection

import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.cast
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions

class EnumClass<E : Any>(declareType: (String) -> Unit, kClass: KClass<E>, getId: (E) -> String) : BaseClass {
	override val name = kClass.simpleName!!

	init {
		declareType(name)
		Util.checkSerializable(kClass)
	}

	private val ids: List<String>

	init {
		val kFunctionValues = kClass.functions.find { it.name == "values" }!!
		val values = kFunctionValues.call() as Array<*>
		ids = values.map { value ->
			val id = getId(kClass.cast(value))
			val jsonId = Json.encodeToString(serializer(kClass.createType()), value)
			if (jsonId != "\"$id\"") {
				Util.throwIllegalStateException("$kClass: SerialName != id")
			}
			id
		}
	}

	override fun append(appendString: (String) -> Unit) {
		appendString("\n\n    export type $name = ")
		appendString(ids.joinToString(" | ") { "'$it'" })
		appendString(";")
	}

	override fun appendDiscriminator(appendString: (String) -> Unit) {
		appendString("\n        | $name")
	}
}