package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*

class CommonSubexpressionElimination(
	private val addCommand: AddCommand,
) : AnyBlockTransformer {
	private val optimization = Optimization.CommonSubexpressionElimination
	private val mappings = mutableListOf<Map<Expression, ReferenceHandler>>()

	companion object : AnyBlockTransformer.Factory {
		override fun createAnyBlockTransformer(addCommand: AddCommand, liveOnExit: Set<Variable>) =
			CommonSubexpressionElimination(addCommand)
	}

	private fun <R> useState(block: () -> R): R {
		val result = block()
		mappings.clear()
		return result
	}

	private fun visitExpression(ctx: ExpressionContext<Expression>): Expression {
		val mapping = mappings[ctx.statement.originalIndex]
		for ((expression, referenceHandler) in mapping) {
			referenceHandler.addAssignment(
				addCommand,
				optimization,
				ctx.statement.basicBlock,
				ctx.castThis(expression).visitExpression(ExpressionReplaceVisitor({}, Replacer(mapping, optimization, true)))
			)
		}
		return ctx.visitExpression(ExpressionReplaceVisitor(addCommand, Replacer(mapping, optimization, false)))
	}

	private fun visitBasicBlock(ctx: BasicBlockContext, expressionCtx: ExpressionContext<Expression>?): BasicBlock {
		val mapping = mutableMapOf<Expression, ReferenceHandler>()
		// First pass
		for ((index, statement) in ctx.original.statements.withIndex()) {
			val visitor = Visitor(mapping)
			when (statement) {
				is Assignment -> {
					ctx.enterAssignmentRhs(statement, index).visitExpression(visitor)
					mappings.add(mapping.toMap())
					if (statement.lhs is VariableAccess) {
						for ((expression, referenceHandler) in mapping.entries.toList()) {
							ctx.enterBasicStatement(null, -1).enterExpression(expression, -1).visitExpression(VariableAccessVisitor { variable ->
								if (variable == statement.lhs.variable) {
									mapping.remove(expression)
								}
							})
							referenceHandler.withVariableAccess { variableAccess ->
								if (variableAccess.variable == statement.lhs.variable) {
									mapping.remove(expression)
								}
							}
						}
						if (statement.lhs.variable.level == null) {
							mapping[statement.rhs] = ReferenceHandler(statement.lhs, true)
						}
					}
				}
				is ExpressionStatement -> {
					ctx.enterExpressionStatementExpression(statement, index).visitExpression(visitor)
					mappings.add(mapping.toMap())
				}
				is VariableDeclarations -> {
					mappings.add(mapping.toMap())
				}
			}
		}
		if (expressionCtx != null) {
			expressionCtx.visitExpression(Visitor(mapping))
			mappings.add(mapping.toMap())
		}
		// Second pass
		return ctx.mapBasicStatements { ctx1 ->
			when (ctx1.original) {
				is Assignment -> Assignment(ctx1.original.lhs, visitExpression(ctx.enterAssignmentRhs(ctx1.original, ctx1.originalIndex)))
				is ExpressionStatement -> ExpressionStatement(visitExpression(ctx.enterExpressionStatementExpression(ctx1.original, ctx1.originalIndex)))
				is VariableDeclarations, null -> ctx1.original
			}
		}
	}

	override fun mapBasicBlock(ctx: BasicBlockContext) =
		useState { visitBasicBlock(ctx, null) }

	override fun mapExpressionBlock(ctx: ExpressionBlockContext) =
		useState { ctx.map({ visitBasicBlock(it, ctx.expression) }, { visitExpression(it) }) }

	private class ReferenceHandler(
		private var variableAccess: VariableAccess?,
		private var assignmentAdded: Boolean
	) {

		fun addAssignment(addCommand: AddCommand, optimization: Optimization, ctx: BasicBlockContext, expression: Expression) {
			withVariableAccess { variableAccess ->
				if (!assignmentAdded) {
					assignmentAdded = true
					addCommand(ctx.addStatement(optimization, Assignment(variableAccess, expression)))
				}
			}
		}

		fun addReference(addTemporaryVariable: () -> Variable) {
			variableAccess = variableAccess ?: VariableAccess(addTemporaryVariable())
		}

		fun <R> withVariableAccess(block: (VariableAccess) -> R) =
			variableAccess?.let(block)
	}

	private class Visitor(
		private val mapping: MutableMap<Expression, ReferenceHandler>,
	) : ExpressionVisitor<Boolean> {
		private fun lookup(ctx: ExpressionContext<Expression>, hasSideEffect: Boolean): Boolean {
			if (hasSideEffect) {
				return true
			}
			val referenceHandler = mapping[ctx.original]
			if (referenceHandler != null) {
				referenceHandler.addReference { ctx.statement.basicBlock.addTemporaryVariable(ctx.original.type ?: Type.langObject) }
			} else {
				mapping[ctx.original] = ReferenceHandler(null, false)
			}
			return false
		}

		override fun visitArithmeticUnaryOperation(operand: Boolean, ctx: ExpressionContext<ArithmeticUnaryOperation>) =
			lookup(ctx, SideEffectVisitor.visitArithmeticUnaryOperation(operand, ctx))

		override fun visitLogicalNotUnaryOperation(operand: Boolean, ctx: ExpressionContext<LogicalNotUnaryOperation>) =
			lookup(ctx, SideEffectVisitor.visitLogicalNotUnaryOperation(operand, ctx))

		override fun visitArithmeticBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<ArithmeticBinaryOperation>) =
			lookup(ctx, SideEffectVisitor.visitArithmeticBinaryOperation(operands, ctx))

		override fun visitLogicalBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<LogicalBinaryOperation>) =
			lookup(ctx, SideEffectVisitor.visitLogicalBinaryOperation(operands, ctx))

		override fun visitRelationalBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<RelationalBinaryOperation>) =
			lookup(ctx, SideEffectVisitor.visitRelationalBinaryOperation(operands, ctx))

		override fun visitObjectEqualsBinaryOperation(operands: BinaryOperands<Boolean>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>) =
			lookup(ctx, SideEffectVisitor.visitObjectEqualsBinaryOperation(operands, ctx))

		override fun visitTernaryOperation(condition: Boolean, operands: BinaryOperands<Boolean>, ctx: ExpressionContext<TernaryOperation>) =
			lookup(ctx, SideEffectVisitor.visitTernaryOperation(condition, operands, ctx))

		override fun visitCoercionExpression(operand: Boolean, ctx: ExpressionContext<CoercionExpression>) =
			lookup(ctx, SideEffectVisitor.visitCoercionExpression(operand, ctx))

		override fun visitBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>) = SideEffectVisitor.visitBooleanLiteral(ctx)
		override fun visitIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>) = SideEffectVisitor.visitIntegerLiteral(ctx)
		override fun visitStringLiteral(ctx: ExpressionContext<StringLiteral>) = SideEffectVisitor.visitStringLiteral(ctx)
		override fun visitNixLiteral(ctx: ExpressionContext<NixLiteral>) = SideEffectVisitor.visitNixLiteral(ctx)
		override fun visitThisExpression(ctx: ExpressionContext<ThisExpression>) = SideEffectVisitor.visitThisExpression(ctx)
		override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>) = SideEffectVisitor.visitVariableAccess(ctx)
		override fun visitMemberAccess(operand: Boolean, ctx: ExpressionContext<MemberAccess>) = SideEffectVisitor.visitMemberAccess(operand, ctx)
		override fun visitBuiltinMethodInvocation(arguments: List<Boolean>, ctx: ExpressionContext<BuiltinMethodInvocation>) = SideEffectVisitor.visitBuiltinMethodInvocation(arguments, ctx)
		override fun visitMethodInvocation(operand: Boolean, arguments: List<Boolean>, ctx: ExpressionContext<MethodInvocation>) = SideEffectVisitor.visitMethodInvocation(operand, arguments, ctx)
		override fun visitObjectAllocation(arguments: List<Boolean>, ctx: ExpressionContext<ObjectAllocation>) = SideEffectVisitor.visitObjectAllocation(arguments, ctx)
	}

	private class Replacer(
		private val mapping: Map<Expression, ReferenceHandler>,
		private val optimization: Optimization,
		private val skipRootExpression: Boolean,
	) : ExpressionReplaceVisitor.Replacer {
		private fun lookup(ctx: ExpressionContext<Expression>, old: Expression) =
			if (skipRootExpression && ctx.isRootExpression()) {
				null
			} else {
				mapping[ctx.original]?.withVariableAccess { ctx.replaceExpression(optimization, old, it, VariableReplaceExpressionReason(it.variable), null) }
			}

		override fun replaceArithmeticUnaryOperation(operand: Expression, ctx: ExpressionContext<ArithmeticUnaryOperation>, alt: ArithmeticUnaryOperation) = lookup(ctx, alt)
		override fun replaceLogicalNotUnaryOperation(operand: Expression, ctx: ExpressionContext<LogicalNotUnaryOperation>, alt: LogicalNotUnaryOperation) = lookup(ctx, alt)
		override fun replaceArithmeticBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ArithmeticBinaryOperation>, alt: ArithmeticBinaryOperation) = lookup(ctx, alt)
		override fun replaceLogicalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<LogicalBinaryOperation>, alt: LogicalBinaryOperation) = lookup(ctx, alt)
		override fun replaceRelationalBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<RelationalBinaryOperation>, alt: RelationalBinaryOperation) = lookup(ctx, alt)
		override fun replaceObjectEqualsBinaryOperation(operands: BinaryOperands<Expression>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>, alt: ObjectEqualsBinaryOperation) = lookup(ctx, alt)
		override fun replaceTernaryOperation(condition: Expression, operands: BinaryOperands<Expression>, ctx: ExpressionContext<TernaryOperation>, alt: TernaryOperation) = lookup(ctx, alt)
		override fun replaceCoercionExpression(operand: Expression, ctx: ExpressionContext<CoercionExpression>, alt: CoercionExpression) = lookup(ctx, alt)
	}
}