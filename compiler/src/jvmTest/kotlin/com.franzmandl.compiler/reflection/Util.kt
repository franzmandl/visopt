package com.franzmandl.compiler.reflection

import com.franzmandl.compiler.ast.Type
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation

object Util {
	private fun getClass(kType: KType) =
		kType.classifier as KClass<*>

	private fun isSerializable(kClass: KClass<*>) =
		kClass.findAnnotation<Serializable>() != null

	private fun getSerialName(kClass: KClass<*>) =
		kClass.findAnnotation<SerialName>()?.value

	fun checkSerializable(kClass: KClass<*>) {
		if (!isSerializable(kClass)) {
			throwIllegalStateException("$kClass: Annotation 'Serializable' missing")
		}
		val serialName = getSerialName(kClass)
		if (DataClass.isDataClass(kClass)) {
			if (serialName != kClass.simpleName) {
				throwIllegalStateException("$kClass: SerialName ($serialName) != simpleName (${kClass.simpleName})")
			}
		} else {
			if (serialName != null) {
				throwIllegalStateException("$kClass: class has SerialName")
			}
		}
	}

	fun getTypeScriptType(kType: KType, useType: (String) -> Unit): String =
		when (val kClass = getClass(kType)) {
			List::class, Set::class -> {
				val type = getTypeScriptType(kType.arguments[0].type!!, useType)
				useType(type)
				"ReadonlyArray<$type>"
			}
			else -> {
				val type = when (kClass) {
					Boolean::class -> "boolean"
					Int::class -> "number"
					String::class, Type::class -> "string"
					else -> kClass.simpleName!!
				}
				useType(type)
				type
			}
		}

	fun getTypeScriptType(kClass: KClass<*>, useType: (String) -> Unit): String =
		getTypeScriptType(kClass.createType(), useType)

	fun throwIllegalStateException(message: String): Nothing {
		throw IllegalStateException(message)
	}
}