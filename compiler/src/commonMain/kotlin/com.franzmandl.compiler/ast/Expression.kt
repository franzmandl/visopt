package com.franzmandl.compiler.ast

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class Expression {
	abstract val type: Type?
}

@Serializable
@SerialName("ArithmeticUnaryOperation")
data class ArithmeticUnaryOperation(
	val operator: ArithmeticUnaryOperator,
	val operand: Expression,
) : Expression() {
	@Transient
	override val type = Type.int

	@Required
	val needsBraces = BraceUtil.needsUnaryBraces(operand, false)
}

@Serializable
@SerialName("LogicalNotUnaryOperation")
data class LogicalNotUnaryOperation(
	val id: Int,
	val operand: Expression,
) : Expression() {
	@Transient
	override val type = Type.bool

	@Required
	val needsBraces = BraceUtil.needsUnaryBraces(operand, true)
}

@Serializable
@SerialName("BinaryOperands")
data class BinaryOperands<T>(val lhs: T, val rhs: T)

@Serializable
sealed class BinaryOperation : Expression() {
	abstract val operator: BinaryOperator
	abstract val operands: BinaryOperands<Expression>
	abstract val needsLhsBraces: Boolean
	abstract val needsRhsBraces: Boolean
}

@Serializable
@SerialName("ArithmeticBinaryOperation")
data class ArithmeticBinaryOperation(
	override val operator: ArithmeticBinaryOperator,
	override val operands: BinaryOperands<Expression>,
) : BinaryOperation() {
	@Transient
	override val type = Type.int

	@Required
	override val needsLhsBraces = BraceUtil.needsBinaryBraces(operator, operands, false)

	@Required
	override val needsRhsBraces = BraceUtil.needsBinaryBraces(operator, operands, true)
}

@Serializable
@SerialName("LogicalBinaryOperation")
data class LogicalBinaryOperation(
	val id: Int,
	override val operator: LogicalBinaryOperator,
	override val operands: BinaryOperands<Expression>,
) : BinaryOperation() {
	@Transient
	override val type = Type.bool

	@Required
	override val needsLhsBraces = BraceUtil.needsBinaryBraces(operator, operands, false)

	@Required
	override val needsRhsBraces = BraceUtil.needsBinaryBraces(operator, operands, true)
}

@Serializable
@SerialName("RelationalBinaryOperation")
data class RelationalBinaryOperation(
	val id: Int,
	override val operator: RelationalBinaryOperator,
	override val operands: BinaryOperands<Expression>,
) : BinaryOperation() {
	@Transient
	override val type = Type.bool

	@Required
	override val needsLhsBraces = BraceUtil.needsBinaryBraces(operator, operands, false)

	@Required
	override val needsRhsBraces = BraceUtil.needsBinaryBraces(operator, operands, true)
}

@Serializable
@SerialName("ObjectEqualsBinaryOperation")
data class ObjectEqualsBinaryOperation(
	val id: Int,
	override val operator: ObjectEqualsBinaryOperator,
	override val operands: BinaryOperands<Expression>,
) : BinaryOperation() {
	@Transient
	override val type = Type.bool

	@Required
	override val needsLhsBraces = BraceUtil.needsBinaryBraces(operator, operands, false)

	@Required
	override val needsRhsBraces = BraceUtil.needsBinaryBraces(operator, operands, true)
}

@Serializable
@SerialName("TernaryOperation")
data class TernaryOperation(
	val id: Int,
	val condition: Expression,
	val operands: BinaryOperands<Expression>,
) : Expression() {
	@Transient
	override val type = operands.lhs.type ?: operands.rhs.type

	@Required
	val needsConditionBraces = BraceUtil.needsTernaryBraces(condition)

	@Required
	val needsLhsBraces = BraceUtil.needsTernaryBraces(operands.lhs)

	@Required
	val needsRhsBraces = BraceUtil.needsTernaryBraces(operands.rhs)
}

@Serializable
@SerialName("CoercionExpression")
data class CoercionExpression(
	val id: Int,
	val operand: Expression,
	val expectedType: Type,
	val acceptedType: Type,
) : Expression() {
	@Transient
	override val type = expectedType
}

@Serializable
sealed class LiteralExpression : Expression()

@Serializable
sealed class BooleanLiteral : LiteralExpression() {
	abstract val value: Boolean

	@Transient
	override val type = Type.bool

	companion object {
		fun of(value: Boolean): BooleanLiteral = if (value) BooleanLiteralTrue else BooleanLiteralFalse
	}
}

@Serializable
@SerialName("BooleanLiteralFalse")
object BooleanLiteralFalse : BooleanLiteral() {
	@Transient
	override val value = false
}

@Serializable
@SerialName("BooleanLiteralTrue")
object BooleanLiteralTrue : BooleanLiteral() {
	@Transient
	override val value = true
}

@Serializable
@SerialName("IntegerLiteral")
data class IntegerLiteral(
	val value: Int,
) : LiteralExpression() {
	@Transient
	override val type = Type.int

	companion object {
		val m1 = IntegerLiteral(-1)
		val p0 = IntegerLiteral(0)
		val p1 = IntegerLiteral(1)
		val p2 = IntegerLiteral(2)
	}
}

@Serializable
@SerialName("StringLiteral")
data class StringLiteral(
	val value: String,  // Inclusive "".
) : LiteralExpression() {
	@Transient
	override val type = Type.string
}

@Serializable
@SerialName("NixLiteral")
object NixLiteral : LiteralExpression() {
	@Transient
	override val type: Type? = null
}

@Serializable
sealed class AssignableExpression : Expression() {
	abstract override val type: Type
}

@Serializable
@SerialName("ThisExpression")
data class ThisExpression(
	override val type: Type,
	val isExplicitly: Boolean,
) : AssignableExpression()

@Serializable
@SerialName("VariableAccess")
data class VariableAccess(
	val variable: Variable,
) : AssignableExpression() {
	@Transient
	override val type = variable.type
}

@Serializable
@SerialName("MemberAccess")
data class MemberAccess(
	val operand: Expression,
	val clazz: Type,
	val member: Member,
) : AssignableExpression() {
	@Transient
	override val type = member.type
}

@Serializable
sealed class InvocationExpression : Expression() {
	abstract val arguments: List<Expression>
	abstract override val type: Type
}

@Serializable
@SerialName("BuiltinMethodInvocation")
data class BuiltinMethodInvocation(
	val method: BuiltinMethod,
	override val arguments: List<Expression>,
) : InvocationExpression() {
	@Transient
	override val type = method.type
}

@Serializable
@SerialName("MethodInvocation")
data class MethodInvocation(
	val operand: Expression,
	val clazz: Type,
	val methodSignature: MethodSignature,
	override val arguments: List<Expression>,
) : InvocationExpression() {
	@Transient
	override val type = methodSignature.returnType
}

@Serializable
@SerialName("ObjectAllocation")
data class ObjectAllocation(
	override val type: Type,
	val constructorSignature: ConstructorSignature,
	override val arguments: List<Expression>,
) : InvocationExpression()