package com.franzmandl.compiler.reflection

import kotlinx.serialization.Required
import kotlinx.serialization.Transient
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty1
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.javaField

class DataClass(declareType: (String) -> Unit, useType: (String) -> Unit, kClass: KClass<*>) : BaseClass {
	override val name = kClass.simpleName!!

	init {
		declareType(name)
		Util.checkSerializable(kClass)
	}

	private val members = kClass.primaryConstructor?.parameters?.mapTo(mutableListOf()) { kParameter ->
		Member(kParameter.name!!, Util.getTypeScriptType(kParameter.type, useType), kParameter.isOptional, kParameter.type.isMarkedNullable)
	} ?: mutableListOf()

	init {
		val primaryMemberNames = members.associateBy { it.name }
		for (kMember in kClass.members) {
			if (kMember !is KProperty1<*, *>) {
				continue
			}
			if (kMember.javaField == null) {  // Detects val someMember get() = ...
				continue
			}
			if (kMember.name in primaryMemberNames) {
				continue
			}
			val isMutable = kMember is KMutableProperty
			val isRequired = kMember.findAnnotation<Required>() != null
			val isTransient = kMember.findAnnotation<Transient>() != null
			val isOk = isTransient xor if (isMutable xor isRequired) {
				members.add(Member(kMember.name, Util.getTypeScriptType(kMember.returnType, useType), !isRequired, kMember.returnType.isMarkedNullable))
			} else {
				false
			}
			if (!isOk) {
				Util.throwIllegalStateException(
					"$kClass: Illegal annotations: Mutable=$isMutable, Required=$isRequired, Transient=$isTransient for member '$kMember'"
				)
			}
		}
	}

	override fun append(appendString: (String) -> Unit) {
		Member.appendMembers(appendString, name, members)
	}

	override fun appendDiscriminator(appendString: (String) -> Unit) {
		if (members.isEmpty()) {
			appendString("\n        | {readonly discriminator: '$name'}")
		} else {
			appendString("\n        | ({readonly discriminator: '$name'} & $name)")
		}
	}

	companion object {
		fun isDataClass(kClass: KClass<*>) =
			kClass.isData || kClass.objectInstance != null
	}
}