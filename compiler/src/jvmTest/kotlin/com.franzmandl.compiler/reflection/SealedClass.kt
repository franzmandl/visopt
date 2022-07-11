package com.franzmandl.compiler.reflection

import kotlin.reflect.KClass

class SealedClass(declareType: (String) -> Unit, useType: (String) -> Unit, kClass: KClass<*>) : BaseClass {
	override val name = kClass.simpleName!!

	init {
		declareType(name)
		Util.checkSerializable(kClass)
	}

	private val children = kClass.sealedSubclasses.map { subclass ->
		if (subclass.isSealed) {
			SealedClass(declareType, useType, subclass)
		} else if (DataClass.isDataClass(subclass)) {
			DataClass(declareType, useType, subclass)
		} else {
			Util.throwIllegalStateException("$kClass > $subclass: Illegal child")
		}
	}

	override fun append(appendString: (String) -> Unit) {
		appendString("\n\n    export type $name =")
		for (child in children) {
			child.appendDiscriminator(appendString)
		}
		appendString(";")
		for (child in children) {
			child.append(appendString)
		}
	}

	override fun appendDiscriminator(appendString: (String) -> Unit) {
		appendString("\n        | $name")
	}
}