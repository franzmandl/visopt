package com.franzmandl.compiler.ast

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TypeSerializer::class)
data class Type(val id: String) {
	@Transient
	val isMain = id == Clazz.mainId

	override fun toString() = id

	companion object {
		val bool = Type("bool")
		val int = Type("int")
		val langObject = Type("java/lang/Object")
		val string = Type("String")

		/**
		 * takeIf prevents "Operator '*' not defined for type(s) 'java/lang/Object', 'int'" for:
		 * tmp1 = nix;
		 * tmp1 * 1;
		 */
		fun toString(type: Type?) = type?.takeIf { it != langObject }?.id ?: "nix"
	}
}

object TypeSerializer : KSerializer<Type> {
	override val descriptor = PrimitiveSerialDescriptor(Type::class.toString(), PrimitiveKind.STRING)
	override fun deserialize(decoder: Decoder) = Type(decoder.decodeString())
	override fun serialize(encoder: Encoder, value: Type) = encoder.encodeString(value.id)
}