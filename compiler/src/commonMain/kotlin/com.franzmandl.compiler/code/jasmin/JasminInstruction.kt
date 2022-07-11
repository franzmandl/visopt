package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.AccessModifier
import com.franzmandl.compiler.ast.Member
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class JasminInstruction {
	abstract val line: LineBuilder
}

@Serializable
sealed class DotInstruction : JasminInstruction()

@Serializable
@SerialName("DotSource")
data class DotSource(val sourceFileName: String) : DotInstruction() {
	@Transient
	override val line = LineBuilder(".source").argument(sourceFileName)
}

@Serializable
@SerialName("DotClass")
data class DotClass(val classId: String) : DotInstruction() {
	@Transient
	override val line = LineBuilder(".class public").argument(classId)
}

@Serializable
@SerialName("DotSuper")
data class DotSuper(val classId: String) : DotInstruction() {
	@Transient
	override val line = LineBuilder(".super").argument(classId)
}

@Serializable
@SerialName("DotLimitLocals")
data class DotLimitLocals(val value: Int) : DotInstruction() {
	@Transient
	override val line = LineBuilder(".limit locals").argument(value)
}

@Serializable
@SerialName("DotLimitStack")
data class DotLimitStack(val value: Int) : DotInstruction() {
	@Transient
	override val line = LineBuilder(".limit stack").argument(value)
}

@Serializable
@SerialName("DotField")
data class DotField(val member: Member, val isStatic: Boolean) : DotInstruction() {
	@Transient
	override val line = LineBuilder(".field").argument(member.accessModifier).argumentStatic(isStatic).argument().memberId(member.id, JasminValueType.create(member.type))
}

@Serializable
@SerialName("DotMethod")
data class DotMethod(val accessModifier: AccessModifier, val isStatic: Boolean, val signature: JasminSignature) : DotInstruction() {
	@Transient
	override val line = LineBuilder(".method").argument(accessModifier).argumentStatic(isStatic).argument().methodId(signature)
}

@Serializable
@SerialName("DotEndMethod")
object DotEndMethod : DotInstruction() {
	@Transient
	override val line = LineBuilder(".end method")
}

sealed interface StackChange {
	val stackConsume: Int
	val stackProduce: Int
}

/** Consumes zero values from stack */
data class StackConsume0(override val stackProduce: Int) : StackChange {
	override val stackConsume = 0
}

/** Consumes one value from stack */
data class StackConsume1(override val stackProduce: Int) : StackChange {
	override val stackConsume = 1
}

/** Consumes two values from stack */
data class StackConsume2(override val stackProduce: Int) : StackChange {
	override val stackConsume = 2
}

/** Consumes variable amount of values from stack */
data class StackConsumeN(
	override val stackConsume: Int,
	override val stackProduce: Int,
) : StackChange

interface ChangesStack<SC : StackChange> {
	val stackChange: SC
}

@Serializable
@SerialName("Label")
data class Label(val name: String) : JasminInstruction(), ChangesStack<StackConsumeN> {
	@Transient
	override val line = LineBuilder("$name:")

	@Transient
	override val stackChange = StackConsumeN(0, 0)
}

@Serializable
@SerialName("EnteringOtherProductionBranch")
object EnteringOtherProductionBranch : ChangesStack<StackConsumeN> {
	@Transient
	override val stackChange = StackConsumeN(1, 0)
}

sealed interface HasVariable {
	val variable: JasminVariable
}

data class DeclareVariable(
	override val variable: JasminVariable,
) : ChangesStack<StackConsumeN>, HasVariable {
	override val stackChange = StackConsumeN(0, 0)
}

data class Load(
	override val variable: JasminVariable,
) : ChangesStack<StackConsume0>, HasVariable {
	override val stackChange = StackConsume0(1)
}

/** Pushes null onto stack. */
@Serializable
@SerialName("AconstNull")
object AconstNull : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("aconst_null")

	@Transient
	override val stackChange = StackConsume0(1)
}

/** Pushes object from array index onto stack. */
@Serializable
@SerialName("Aload")
data class Aload(val index: Int, val id: String) : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("aload").argument(index).comment(id)

	@Transient
	override val stackChange = StackConsume0(1)
}

/** Pushes this onto stack. */
@Serializable
@SerialName("Aload0")
object Aload0 : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("aload_0")

	@Transient
	override val stackChange = StackConsume0(1)
}

@Serializable
@SerialName("Getstatic")
data class Getstatic(val arguments: String) : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("getstatic").argument(arguments)

	@Transient
	override val stackChange = StackConsume0(1)
}

@Serializable
@SerialName("Goto")
data class Goto(val label: Label) : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("goto").argument(label)

	@Transient
	override val stackChange = StackConsume0(0)
}

/** Pushes integer from array index onto stack. */
@Serializable
@SerialName("Iload")
data class Iload(val index: Int, val id: String) : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("iload").argument(index).comment(id)

	@Transient
	override val stackChange = StackConsume0(1)
}

@Serializable
sealed class Ldc : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val stackChange = StackConsume0(1)
}

/** Pushes constant integer onto stack. */
@Serializable
@SerialName("LdcInt")
data class LdcInt(val value: Int) : Ldc() {
	@Transient
	override val line = LineBuilder("ldc").argument(value)
}

/** Pushes constant string onto stack. */
@Serializable
@SerialName("LdcString")
data class LdcString(val value: String) : Ldc() {
	@Transient
	override val line = LineBuilder("ldc").argument(value)
}

@Serializable
@SerialName("New")
data class New(val arguments: String) : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("new").argument(arguments)

	@Transient
	override val stackChange = StackConsume0(1)
}

@Serializable
@SerialName("Return")
object Return : JasminInstruction(), ChangesStack<StackConsume0> {
	@Transient
	override val line = LineBuilder("return")

	@Transient
	override val stackChange = StackConsume0(0)
}

data class Store(
	override val variable: JasminVariable,
) : ChangesStack<StackConsume1>, HasVariable {
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Areturn")
object Areturn : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("areturn")

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Astore")
data class Astore(val index: Int, val id: String) : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("astore").argument(index).comment(id)

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Astore0")
object Astore0 : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("astore_0")

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Dup")
object Dup : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("dup")

	@Transient
	override val stackChange = StackConsume1(2)
}

@Serializable
@SerialName("Getfield")
data class Getfield(val classId: String, val id: String, val type: JasminType) : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("getfield").argument().member(classId, id, type)

	@Transient
	override val stackChange = StackConsume1(1)
}

/** If value == 0 */
@Serializable
@SerialName("Ifeq")
data class Ifeq(val label: Label) : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("ifeq").argument(label)

	@Transient
	override val stackChange = StackConsume1(0)
}

/** If value != 0 */
@Serializable
@SerialName("Ifne")
data class Ifne(val label: Label) : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("ifne").argument(label)

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Ineg")
object Ineg : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("ineg")

	@Transient
	override val stackChange = StackConsume1(1)
}

@Serializable
@SerialName("Ireturn")
object Ireturn : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("ireturn")

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Istore")
data class Istore(val index: Int, val id: String) : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("istore").argument(index).comment(id)

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Pop")
object Pop : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("pop")

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Putstatic")
data class Putstatic(val arguments: String) : JasminInstruction(), ChangesStack<StackConsume1> {
	@Transient
	override val line = LineBuilder("putstatic").argument(arguments)

	@Transient
	override val stackChange = StackConsume1(0)
}

@Serializable
@SerialName("Iadd")
object Iadd : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("iadd")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Iand")
object Iand : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("iand")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Idiv")
object Idiv : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("idiv")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("IfAcmpeq")
data class IfAcmpeq(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_acmpeq").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("IfAcmpne")
data class IfAcmpne(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_acmpne").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("IfIcmpeq")
data class IfIcmpeq(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_icmpeq").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("IfIcmpne")
data class IfIcmpne(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_icmpne").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("IfIcmpge")
data class IfIcmpge(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_icmpge").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("IfIcmpgt")
data class IfIcmpgt(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_icmpgt").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("IfIcmple")
data class IfIcmple(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_icmple").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("IfIcmplt")
data class IfIcmplt(val label: Label) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("if_icmplt").argument(label)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("Imul")
object Imul : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("imul")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Ior")
object Ior : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("ior")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Irem")
object Irem : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("irem")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Ishl")
object Ishl : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("ishl")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Ishr")
object Ishr : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("ishr")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Isub")
object Isub : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("isub")

	@Transient
	override val stackChange = StackConsume2(1)
}

@Serializable
@SerialName("Putfield")
data class Putfield(val classId: String, val id: String, val type: JasminType) : JasminInstruction(), ChangesStack<StackConsume2> {
	@Transient
	override val line = LineBuilder("putfield").argument().member(classId, id, type)

	@Transient
	override val stackChange = StackConsume2(0)
}

@Serializable
@SerialName("Invokespecial")
data class Invokespecial(val classId: String, val argumentTypes: List<JasminValueType>) : JasminInstruction(), ChangesStack<StackConsumeN> {
	@Transient
	override val line = LineBuilder("invokespecial").argument().method(classId, JasminSignature.createInitSignature(argumentTypes))

	@Transient
	override val stackChange = StackConsumeN(
		argumentTypes.size + 1,  // + 1 for this
		0
	)
}

@Serializable
@SerialName("Invokestatic")
data class Invokestatic(val classId: String, val signature: JasminSignature) : JasminInstruction(), ChangesStack<StackConsumeN> {
	@Transient
	override val line = LineBuilder("invokestatic").argument().method(classId, signature)

	@Transient
	override val stackChange = StackConsumeN(
		signature.argumentTypes.size,
		if (signature.returnType != JasminVoid) 1 else 0
	)
}

@Serializable
@SerialName("Invokevirtual")
data class Invokevirtual(val classId: String, val signature: JasminSignature) : JasminInstruction(), ChangesStack<StackConsumeN> {
	@Transient
	override val line = LineBuilder("invokevirtual").argument().method(classId, signature)

	@Transient
	override val stackChange = StackConsumeN(
		signature.argumentTypes.size + 1,  // + 1 for this
		if (signature.returnType != JasminVoid) 1 else 0
	)
}