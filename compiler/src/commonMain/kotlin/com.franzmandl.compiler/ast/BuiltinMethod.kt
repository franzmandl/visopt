package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class BuiltinMethod {
	abstract val signature: Signature
	abstract val type: Type
}

@Serializable
sealed class BuiltinPrintMethod : BuiltinMethod()

@Serializable
@SerialName("PrintBoolMethod")
object PrintBoolMethod : BuiltinPrintMethod() {
	@Transient
	override val signature = Signature("print", listOf(Type.bool))

	@Transient
	override val type = Type.int
}

@Serializable
@SerialName("PrintIntMethod")
object PrintIntMethod : BuiltinPrintMethod() {
	@Transient
	override val signature = Signature("print", listOf(Type.int))

	@Transient
	override val type = Type.int
}

@Serializable
@SerialName("PrintStringMethod")
object PrintStringMethod : BuiltinPrintMethod() {
	@Transient
	override val signature = Signature("print", listOf(Type.string))

	@Transient
	override val type = Type.int
}

@Serializable
sealed class BuiltinReadMethod : BuiltinMethod()

@Serializable
@SerialName("ReadIntMethod")
object ReadIntMethod : BuiltinReadMethod() {
	@Transient
	override val signature = Signature("readInt", listOf())

	@Transient
	override val type = Type.int
}

@Serializable
@SerialName("ReadStringMethod")
object ReadStringMethod : BuiltinReadMethod() {
	@Transient
	override val signature = Signature("readString", listOf())

	@Transient
	override val type = Type.string
}