package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.optimizer.Optimization

class ExpressionContext<out E : Expression>(
	private val rootIndex: Int,
	private val indices: List<Int>,
	val original: E,
	val statement: BasicStatementContext,
) {
	private val currentAddress: ExpressionAddress
		get() = ExpressionAddress(statement.basicBlock.getCurrentBasicStatementAddress(), rootIndex, indices)

	fun <P> address(payload: P) =
		Addressed(currentAddress, payload)

	fun isRootExpression() = indices.isEmpty()

	fun replaceExpression(optimization: Optimization, old: Expression, replacement: Expression, reason: ReplaceExpressionReason, addStatement: AddBasicStatement?) =
		ReplaceExpression(optimization, statement.basicBlock.compound.body.createBodyInfoChange(), currentAddress, old, replacement, reason, addStatement)

	private fun <E1 : Expression> enterExpression(expression: E1, index: Int) =
		ExpressionContext(rootIndex, indices + listOf(index), expression, statement)

	fun mapExpression(address: ExpressionAddress, transform: (ExpressionContext<Expression>) -> Expression): Expression {
		if (address.head == null) {
			return transform(this)
		}

		fun mapBinaryOperands(binaryOperands: BinaryOperands<Expression>, head: Int, tail: ExpressionAddress, firstIndex: Int, transform: (ExpressionContext<Expression>) -> Expression) =
			when (head - firstIndex) {
				0 -> BinaryOperands(enterExpression(binaryOperands.lhs, head).mapExpression(tail, transform), binaryOperands.rhs)
				1 -> BinaryOperands(binaryOperands.lhs, enterExpression(binaryOperands.rhs, head).mapExpression(tail, transform))
				else -> throw IllegalStateException("BinaryOperands: $head")
			}

		fun mapExpressions(expressions: List<Expression>, head: Int, tail: ExpressionAddress, firstIndex: Int, transform: (ExpressionContext<Expression>) -> Expression) =
			Util.transformGuarded { applyTransform ->
				val relativeHead = head - firstIndex
				expressions.mapIndexed { index, expression ->
					if (relativeHead != index) {
						expression
					} else {
						applyTransform()
						enterExpression(expression, head).mapExpression(tail, transform)
					}
				}
			}

		return when (val e: Expression = original) {
			is ArithmeticUnaryOperation -> ArithmeticUnaryOperation(e.operator, enterExpression(e.operand, 0).mapExpression(address.tail, transform))
			is LogicalNotUnaryOperation -> LogicalNotUnaryOperation(e.id, enterExpression(e.operand, 0).mapExpression(address.tail, transform))
			is ArithmeticBinaryOperation -> ArithmeticBinaryOperation(e.operator, mapBinaryOperands(e.operands, address.head, address.tail, 0, transform))
			is LogicalBinaryOperation -> LogicalBinaryOperation(e.id, e.operator, mapBinaryOperands(e.operands, address.head, address.tail, 0, transform))
			is RelationalBinaryOperation -> RelationalBinaryOperation(e.id, e.operator, mapBinaryOperands(e.operands, address.head, address.tail, 0, transform))
			is ObjectEqualsBinaryOperation -> ObjectEqualsBinaryOperation(e.id, e.operator, mapBinaryOperands(e.operands, address.head, address.tail, 0, transform))
			is TernaryOperation ->
				if (address.head == 0) {
					TernaryOperation(e.id, enterExpression(e.condition, 0).mapExpression(address.tail, transform), e.operands)
				} else {
					TernaryOperation(e.id, e.condition, mapBinaryOperands(e.operands, address.head, address.tail, 1, transform))
				}
			is CoercionExpression -> CoercionExpression(e.id, enterExpression(e.operand, 0).mapExpression(address.tail, transform), e.expectedType, e.acceptedType)
			is LiteralExpression -> throw IllegalStateException("Address lead to literal.")
			is AssignableExpression -> throw IllegalStateException("Address lead to assignable expression.")
			is BuiltinMethodInvocation -> BuiltinMethodInvocation(e.method, mapExpressions(e.arguments, address.head, address.tail, 0, transform))
			is MethodInvocation ->
				if (address.head == 0) {
					MethodInvocation(enterExpression(e.operand, 0).mapExpression(address.tail, transform), e.clazz, e.methodSignature, e.arguments)
				} else {
					MethodInvocation(e.operand, e.clazz, e.methodSignature, mapExpressions(e.arguments, address.head, address.tail, 1, transform))
				}
			is ObjectAllocation -> ObjectAllocation(e.type, e.constructorSignature, mapExpressions(e.arguments, address.head, address.tail, 0, transform))
		}
	}

	fun <T> visitExpression(visitor: ExpressionVisitor<T>): T {

		fun visitBinaryOperands(operands: BinaryOperands<Expression>, firstIndex: Int) =
			BinaryOperands(enterExpression(operands.lhs, firstIndex).visitExpression(visitor), enterExpression(operands.rhs, firstIndex + 1).visitExpression(visitor))

		fun visitExpressions(expressions: List<Expression>, firstIndex: Int) =
			expressions.mapIndexed { index, expression -> enterExpression(expression, firstIndex + index).visitExpression(visitor) }

		return when (val e: Expression = original) {
			is ArithmeticUnaryOperation -> visitor.visitArithmeticUnaryOperation(enterExpression(e.operand, 0).visitExpression(visitor), castThis(e))
			is LogicalNotUnaryOperation -> visitor.visitLogicalNotUnaryOperation(enterExpression(e.operand, 0).visitExpression(visitor), castThis(e))
			is ArithmeticBinaryOperation -> visitor.visitArithmeticBinaryOperation(visitBinaryOperands(e.operands, 0), castThis(e))
			is LogicalBinaryOperation -> visitor.visitLogicalBinaryOperation(visitBinaryOperands(e.operands, 0), castThis(e))
			is RelationalBinaryOperation -> visitor.visitRelationalBinaryOperation(visitBinaryOperands(e.operands, 0), castThis(e))
			is ObjectEqualsBinaryOperation -> visitor.visitObjectEqualsBinaryOperation(visitBinaryOperands(e.operands, 0), castThis(e))
			is TernaryOperation -> visitor.visitTernaryOperation(enterExpression(e.condition, 0).visitExpression(visitor), visitBinaryOperands(e.operands, 1), castThis(e))
			is CoercionExpression -> visitor.visitCoercionExpression(enterExpression(e.operand, 0).visitExpression(visitor), castThis(e))
			is BooleanLiteral -> visitor.visitBooleanLiteral(castThis(e))
			is IntegerLiteral -> visitor.visitIntegerLiteral(castThis(e))
			is StringLiteral -> visitor.visitStringLiteral(castThis(e))
			is NixLiteral -> visitor.visitNixLiteral(castThis(e))
			is ThisExpression -> visitor.visitThisExpression(castThis(e))
			is VariableAccess -> visitor.visitVariableAccess(castThis(e))
			is MemberAccess -> visitor.visitMemberAccess(enterExpression(e.operand, 0).visitExpression(visitor), castThis(e))
			is BuiltinMethodInvocation -> visitor.visitBuiltinMethodInvocation(visitExpressions(e.arguments, 0), castThis(e))
			is MethodInvocation -> visitor.visitMethodInvocation(enterExpression(e.operand, 0).visitExpression(visitor), visitExpressions(e.arguments, 1), castThis(e))
			is ObjectAllocation -> visitor.visitObjectAllocation(visitExpressions(e.arguments, 0), castThis(e))
		}
	}

	fun <E1 : Expression> castThis(expression: E1) = ExpressionContext(rootIndex, indices, expression, statement)
}