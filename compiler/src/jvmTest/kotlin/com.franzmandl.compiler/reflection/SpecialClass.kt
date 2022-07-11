package com.franzmandl.compiler.reflection

import kotlin.reflect.KClass

class SpecialClass(declareType: (String) -> Unit, kClass: KClass<*>, private val members: List<Member>) : BaseClass {
	override val name = kClass.simpleName!!

	init {
		declareType(name)
		Util.checkSerializable(kClass)
	}

	override fun append(appendString: (String) -> Unit) {
		Member.appendMembers(appendString, name, members)
	}

	override fun appendDiscriminator(appendString: (String) -> Unit) {
		appendString("\n        | ({readonly discriminator: '$name'} & $name)")
	}
}