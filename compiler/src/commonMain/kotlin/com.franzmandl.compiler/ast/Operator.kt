package com.franzmandl.compiler.ast

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ArithmeticUnaryOperator(val sign: String) {
	@SerialName("-")
	Minus("-"),

	@SerialName("+")
	Plus("+"),
	;

	companion object {
		val operators = values().associateBy { it.sign }
	}
}

object LogicalUnaryOperator {
	const val notSign = "!"
}

interface BinaryOperator {
	val order: Int
	val sign: String
}

object OperatorOrder {
	const val multiplication = 100
	const val shift = 90
	const val addition = 80
	const val relational = 70
	const val logicalAnd = 60
	const val logicalOr = 50
}

@Serializable
enum class ArithmeticBinaryOperator(override val sign: String, override val order: Int) : BinaryOperator {
	@SerialName("-")
	Minus("-", OperatorOrder.addition),

	@SerialName("%")
	Percent("%", OperatorOrder.multiplication),

	@SerialName("+")
	Plus("+", OperatorOrder.addition),

	@SerialName("<<")
	ShiftLeft("<<", OperatorOrder.shift),

	@SerialName(">>")
	ShiftRight(">>", OperatorOrder.shift),

	@SerialName("/")
	Slash("/", OperatorOrder.multiplication),

	@SerialName("*")
	Star("*", OperatorOrder.multiplication),
	;

	companion object {
		val operators = values().associateBy { it.sign }
	}
}

@Serializable
enum class LogicalBinaryOperator(override val sign: String, override val order: Int) : BinaryOperator {
	@SerialName("&&")
	And("&&", OperatorOrder.logicalAnd),

	@SerialName("||")
	Or("||", OperatorOrder.logicalOr),
	;

	companion object {
		val operators = values().associateBy { it.sign }
	}
}

@Serializable
enum class RelationalBinaryOperator(override val sign: String) : BinaryOperator {
	@SerialName("==")
	Equal("=="),

	@SerialName("!=")
	EqualNot("!="),

	@SerialName(">=")
	GreaterEqual(">="),

	@SerialName(">")
	Greater(">"),

	@SerialName("<=")
	SmallerEqual("<="),

	@SerialName("<")
	Smaller("<"),
	;

	override val order = OperatorOrder.relational

	companion object {
		val operators = values().associateBy { it.sign }
	}
}

@Serializable
enum class ObjectEqualsBinaryOperator(override val sign: String) : BinaryOperator {
	@SerialName("==")
	Equal("=="),

	@SerialName("!=")
	EqualNot("!="),
	;

	override val order = OperatorOrder.relational

	companion object {
		val operators = values().associateBy { it.sign }
	}
}