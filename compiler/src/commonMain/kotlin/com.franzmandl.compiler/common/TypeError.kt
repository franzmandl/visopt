package com.franzmandl.compiler.common

import com.franzmandl.compiler.ast.Signature
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class TypeError : PhaseMessage() {
	@Serializable
	@SerialName("BinaryTypeError")
	data class BinaryTypeError(
		override val location: Location,
		private val lhsType: String,
		private val rhsType: String,
		private val operator: String,
	) : TypeError() {
		// Operator '<OP>' not defined for type(s) '<lhs-type>', '<rhs-type>'
		@Required
		override val text = "Operator '$operator' not defined for type(s) '$lhsType', '$rhsType'"
	}

	@Serializable
	@SerialName("CannotInvokeError")
	data class CannotInvokeError(
		override val location: Location,
		private val classId: String,
		private val signatureNullable: SignatureNullable,
	) : TypeError() {
		// Cannot invoke method '<method signature>' on type '<class ID>'
		@Required
		override val text = "Cannot invoke method '$signatureNullable' on type '$classId'"
	}

	@Serializable
	@SerialName("ClassDoubleDefinitionTypeError")
	data class ClassDoubleDefinitionTypeError(
		override val location: Location,
		private val classId: String,
	) : TypeError() {
		// Cannot invoke method '<method signature>' on type '<class ID>'
		@Required
		override val text = "Double definition of class '$classId'"
	}

	@Serializable
	@SerialName("DoesNotHaveMemberError")
	data class DoesNotHaveMemberError(
		override val location: Location,
		private val classId: String,
		private val memberId: String,
	) : TypeError() {
		// '<class ID>' does not have field '<member ID>'
		@Required
		override val text = "'$classId' does not have field '$memberId'"
	}

	@Serializable
	@SerialName("IncompatibleConditionTypeError")
	data class IncompatibleConditionTypeError(
		override val location: Location,
		private val type: String,
	) : TypeError() {
		// Incompatible type '<type>' for condition
		@Required
		override val text = "Incompatible type '$type' for condition"
	}

	@Serializable
	@SerialName("IncompatibleReturnTypeError")
	data class IncompatibleReturnTypeError(
		override val location: Location,
		private val type: String,
	) : TypeError() {
		// Incompatible type '<type>' for return
		@Required
		override val text = "Incompatible type '$type' for return"
	}

	@Serializable
	@SerialName("MainInstantiationError")
	data class MainInstantiationError(
		override val location: Location,
	) : TypeError() {
		// Main cannot be used as a type
		@Required
		override val text = "Main cannot be used as a type"
	}

	@Serializable
	@SerialName("MainMemberError")
	data class MainMemberError(
		override val location: Location,
	) : TypeError() {
		// Cannot add member to class Main
		@Required
		override val text = "Cannot add member to class Main"
	}

	@Serializable
	@SerialName("MemberAccessError")
	data class MemberAccessError(
		override val location: Location,
		private val memberId: String,
		private val classId: String,
	) : TypeError() {
		// Field '<member ID>' from type '<class ID>' not visible
		@Required
		override val text = "Field '$memberId' from type '$classId' not visible"
	}

	@Serializable
	@SerialName("MemberDoubleDefinitionTypeError")
	data class MemberDoubleDefinitionTypeError(
		override val location: Location,
		private val memberId: String,
		private val memberType: String,
		private val classId: String,
	) : TypeError() {
		// Double definition of member '<member type> <member ID>' in class '<class ID>'
		@Required
		override val text = "Double definition of member '$memberType $memberId' in class '$classId'"
	}

	@Serializable
	@SerialName("MethodAccessError")
	data class MethodAccessError(
		override val location: Location,
		private val signature: Signature,
		private val classId: String,
	) : TypeError() {
		// Method '<method signature>' from type '<class ID>' not visible
		@Required
		override val text = "Method '$signature' from type '$classId' not visible"
	}

	@Serializable
	@SerialName("MethodDoubleDefinitionTypeError")
	data class MethodDoubleDefinitionTypeError(
		override val location: Location,
		private val signature: Signature,
		private val classId: String,
	) : TypeError() {
		// Double definition of method '<method signature>' in class '<class ID>'
		@Required
		override val text = "Double definition of method '$signature' in class '$classId'"
	}

	@Serializable
	@SerialName("UnaryTypeError")
	data class UnaryTypeError(
		override val location: Location,
		private val type: String,
		private val operator: String,
	) : TypeError() {
		// Operator '<OP>' not defined for type '<type>'
		@Required
		override val text = "Operator '$operator' not defined for type '$type'"
	}

	@Serializable
	@SerialName("UndefinedIdError")
	data class UndefinedIdError(
		override val location: Location,
		private val id: String,
	) : TypeError() {
		// Usage of undefined identifier '<ID>'
		@Required
		override val text = "Usage of undefined identifier '$id'"
	}

	@Serializable
	@SerialName("UndefinedMethodError")
	data class UndefinedMethodError(
		override val location: Location,
		private val signatureNullable: SignatureNullable,
	) : TypeError() {
		// Call to undefined method '<method signature>'
		@Required
		override val text = "Call to undefined method '$signatureNullable'"
	}

	@Serializable
	@SerialName("UnknownTypeError")
	data class UnknownTypeError(
		override val location: Location,
		private val classId: String,
	) : TypeError() {
		// Usage of undefined type '<class ID>'
		@Required
		override val text = "Usage of undefined type '$classId'"
	}

	@Serializable
	@SerialName("VariableDoubleDefinitionTypeError")
	data class VariableDoubleDefinitionTypeError(
		override val location: Location,
		private val variableId: String,
		private val variableType: String,
		private val signature: Signature,
	) : TypeError() {
		// Double definition of variable '<variable type> <variable ID>' in method '<method signature>'
		@Required
		override val text = "Double definition of variable '$variableType $variableId' in method '$signature'"
	}
}