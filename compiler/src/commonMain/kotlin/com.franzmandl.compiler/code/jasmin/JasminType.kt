package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.Type
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class JasminType {
	abstract val id: String
}

@Serializable
sealed class JasminValueType : JasminType() {
	abstract fun load(index: Int, id: String): JasminInstruction
	abstract fun store(index: Int, id: String): JasminInstruction
	abstract fun return1(): ChangesStack<StackConsume1>

	companion object {
		fun create(type: Type): JasminValueType = when (type) {
			Type.bool -> JasminBool
			Type.int -> JasminInt
			Type.string -> JasminReference.string
			else -> JasminReference("L" + type.id + ";")
		}
	}
}

@Serializable
@SerialName("JasminBool")
object JasminBool : JasminValueType() {
	@Transient
	override val id = "Z"
	override fun load(index: Int, id: String) = Iload(index, id)
	override fun store(index: Int, id: String) = Istore(index, id)
	override fun return1() = Ireturn
}

@Serializable
@SerialName("JasminInt")
object JasminInt : JasminValueType() {
	@Transient
	override val id = "I"
	override fun load(index: Int, id: String) = Iload(index, id)
	override fun store(index: Int, id: String) = Istore(index, id)
	override fun return1() = Ireturn
}

@Serializable
@SerialName("JasminReference")
data class JasminReference(override val id: String) : JasminValueType() {
	override fun load(index: Int, id: String) = Aload(index, id)
	override fun store(index: Int, id: String) = Astore(index, id)
	override fun return1() = Areturn

	companion object {
		val inputStream = JasminReference("Ljava/io/InputStream;")
		val string = JasminReference("Ljava/lang/String;")
		val stringArray = JasminReference("[Ljava/lang/String;")
	}
}

@Serializable
@SerialName("JasminVoid")
object JasminVoid : JasminType() {
	@Transient
	override val id = "V"
}