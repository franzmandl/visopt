package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*

class ScopeBuilder(
	private val appendInstruction: (Addressed<out ChangesStack<out StackChange>>) -> Unit,
	private val isMain: Boolean,
	private val returnType: JasminType,
) : ExpressionVisitor<AddInstructions> {
	fun appendBody(ctx: BodyContext, needsScanner: Boolean) {
		for (argument in ctx.original.arguments) {
			appendInstruction(ctx.address(DeclareVariable(JasminVariable(argument.id, 0, JasminValueType.create(argument.type)))))
		}
		if (isMain && needsScanner) {
			appendInstruction(ctx.address(New("java/util/Scanner")))
			appendInstruction(ctx.address(Dup))
			appendInstruction(ctx.address(Getstatic("java/lang/System/in Ljava/io/InputStream;")))
			appendInstruction(ctx.address(Invokespecial("java/util/Scanner", listOf(JasminReference.inputStream))))
			appendInstruction(ctx.address(Putstatic("Main/scanner Ljava/util/Scanner;")))
		}
		appendCompound(ctx.compound)
	}

	private fun appendCompound(ctx: CompoundContext) {
		ctx.mapCompoundStatements { ctx1 ->
			val cfgNode = ctx.body.original.cfg.get(ctx1.original.id)
			when (ctx1.original) {
				is BasicBlock -> {
					val ctx2 = ctx.enterBasicBlock(ctx1.original)
					appendBasicStatements(ctx2, cfgNode.hasComplexPredecessors())
					cfgNode.complexSuccessor?.let { complexSuccessor ->
						appendStack0Instruction(ctx2.address(Goto(Label("L$complexSuccessor"))))
					}
				}
				is IfStatement -> {
					appendExpressionBlock(ctx.enterExpressionBlock(ctx1.original), cfgNode)
					ScopeBuilder(appendInstruction, isMain, returnType).appendCompound(ctx.enterThenBranch(ctx1.original))
					ctx.enterElseBranch(ctx1.original)?.let { ctx2 -> ScopeBuilder(appendInstruction, isMain, returnType).appendCompound(ctx2) }
				}
				is ReturnStatement -> appendReturnStatement(ctx.enterExpressionBlock(ctx1.original), cfgNode)
				is WhileStatement -> {
					appendExpressionBlock(ctx.enterExpressionBlock(ctx1.original), cfgNode)
					ScopeBuilder(appendInstruction, isMain, returnType).appendCompound(ctx.enterWhileBranch(ctx1.original))
				}
			}
			ctx1.original
		}
	}

	private fun appendReturnStatement(ctx: ExpressionBlockContext, cfgNode: CfgNode) {
		appendBasicStatements(ctx.basicBlock, cfgNode.hasComplexPredecessors())
		val lastStatement = BasicStatementContext(ctx.basicBlock, ctx.basicBlock.original.statements.size, null)
		if (isMain) {
			appendExpression(ctx.expression).addInstructions()
			appendInstruction(ctx.basicBlock.address(Invokestatic("java/lang/System", JasminSignature("exit", listOf(JasminInt), JasminVoid))))
			appendStack0Instruction(ctx.basicBlock.address(Return))
		} else if (returnType is JasminValueType) {
			appendStack1Instruction(appendExpression(ctx.expression), lastStatement.address(returnType.return1()))
		}
	}

	private fun appendExpressionBlock(ctx: ExpressionBlockContext, cfgNode: CfgNode) {
		appendBasicStatements(ctx.basicBlock, cfgNode.hasComplexPredecessors())
		val lastStatement = BasicStatementContext(ctx.basicBlock, ctx.basicBlock.original.statements.size, null)
		val lastExpression = appendExpression(ctx.expression)
		if (cfgNode.naturalSuccessor == null || cfgNode.complexSuccessor == null) {
			appendStack1Instruction(lastExpression, lastStatement.address(Pop))
			cfgNode.complexSuccessor?.let { complexSuccessor ->
				appendStack0Instruction(lastStatement.address(Goto(Label("L$complexSuccessor"))))
			}
		} else if (cfgNode.inverted) {
			appendStack1Instruction(lastExpression, lastStatement.address(Ifne(Label("L" + cfgNode.complexSuccessor))))
		} else {
			appendStack1Instruction(lastExpression, lastStatement.address(Ifeq(Label("L" + cfgNode.complexSuccessor))))
		}
		if (cfgNode.selfSuccessor) {
			appendStack0Instruction(ctx.basicBlock.address(Goto(Label("L" + cfgNode.id))))
		}
	}

	private fun appendBasicStatements(ctx: BasicBlockContext, appendLabel: Boolean) {
		if (appendLabel) {
			appendInstruction(ctx.address(Label("L" + ctx.original.id)))
		}
		ctx.mapBasicStatements { ctx1 ->
			when (ctx1.original) {
				is Assignment -> {
					val rhsCtx = ctx.enterAssignmentRhs(ctx1.original, ctx1.originalIndex)
					when (ctx1.original.lhs) {
						is ThisExpression -> appendStack1Instruction(appendExpression(rhsCtx), ctx1.address(Astore0))
						is VariableAccess -> appendStack1Instruction(appendExpression(rhsCtx), ctx1.address(Store(JasminVariable(ctx1.original.lhs.variable))))
						is MemberAccess -> appendStack2Instruction(
							appendExpression(ctx1.enterExpression(ctx1.original.lhs.operand, 0)),
							appendExpression(rhsCtx),
							ctx1.address(Putfield(ctx1.original.lhs.clazz.id, ctx1.original.lhs.member.id, JasminValueType.create(ctx1.original.lhs.member.type))),
						)
					}
				}
				is ExpressionStatement -> {
					val ctx2 = ctx.enterExpressionStatementExpression(ctx1.original, ctx1.originalIndex)
					if (ctx2.original is BuiltinMethodInvocation && ctx2.original.method is BuiltinPrintMethod) {
						appendBuiltinPrintMethodInvocation(
							ctx2, ctx2.original, ctx2.original.arguments.mapIndexed { index, argument -> appendExpression(ExpressionContext(0, listOf(index), argument, ctx1)) }
						)
					} else {
						appendStack1Instruction(appendExpression(ctx2), ctx1.address(Pop))
					}
				}
				is VariableDeclarations -> {
					for (variable in ctx1.original.variables) {
						when (variable.type) {
							Type.bool, Type.int -> appendInstruction(ctx1.address(LdcInt(0)))
							else -> appendInstruction(ctx1.address(AconstNull))
						}
						appendInstruction(ctx1.address(Store(JasminVariable(variable))))
					}
				}
				null -> {}
			}
			ctx1.original
		}
	}

	private fun appendStack0Instruction(instruction: Addressed<out ChangesStack<StackConsume0>>) {
		appendInstruction(instruction)
	}

	private fun appendStack1Instruction(expression: AddInstructions, instruction: Addressed<out ChangesStack<StackConsume1>>) {
		expression.addInstructions()
		appendInstruction(instruction)
	}

	private fun appendStack2Instruction(expression1: AddInstructions, expression2: AddInstructions, instruction: Addressed<out ChangesStack<StackConsume2>>) {
		expression1.addInstructions()
		expression2.addInstructions()
		appendInstruction(instruction)
	}

	private fun appendStack2Instruction(binaryOperands: BinaryOperands<AddInstructions>, instruction: Addressed<out ChangesStack<StackConsume2>>) =
		appendStack2Instruction(binaryOperands.lhs, binaryOperands.rhs, instruction)

	private fun appendExpression(ctx: ExpressionContext<Expression>) =
		ctx.visitExpression(this)

	override fun visitArithmeticUnaryOperation(operand: AddInstructions, ctx: ExpressionContext<ArithmeticUnaryOperation>) = AddInstructions {
		when (ctx.original.operator) {
			ArithmeticUnaryOperator.Minus -> appendStack1Instruction(operand, ctx.address(Ineg))
			ArithmeticUnaryOperator.Plus -> operand.addInstructions()
		}
	}

	override fun visitLogicalNotUnaryOperation(operand: AddInstructions, ctx: ExpressionContext<LogicalNotUnaryOperation>) = AddInstructions {
		appendConditionEvaluation(ctx, "NOT" + ctx.original.id) { appendStack1Instruction(operand, ctx.address(Ifeq(it))) }
	}

	override fun visitArithmeticBinaryOperation(operands: BinaryOperands<AddInstructions>, ctx: ExpressionContext<ArithmeticBinaryOperation>) = AddInstructions {
		appendStack2Instruction(
			operands, ctx.address(
				when (ctx.original.operator) {
					ArithmeticBinaryOperator.Minus -> Isub
					ArithmeticBinaryOperator.Percent -> Irem
					ArithmeticBinaryOperator.Plus -> Iadd
					ArithmeticBinaryOperator.ShiftLeft -> Ishl
					ArithmeticBinaryOperator.ShiftRight -> Ishr
					ArithmeticBinaryOperator.Slash -> Idiv
					ArithmeticBinaryOperator.Star -> Imul
				}
			)
		)
	}

	override fun visitLogicalBinaryOperation(operands: BinaryOperands<AddInstructions>, ctx: ExpressionContext<LogicalBinaryOperation>) = AddInstructions {
		appendStack2Instruction(
			operands.lhs, operands.rhs, ctx.address(
				when (ctx.original.operator) {
					LogicalBinaryOperator.And -> Iand
					LogicalBinaryOperator.Or -> Ior
				}
			)
		)
	}

	override fun visitRelationalBinaryOperation(operands: BinaryOperands<AddInstructions>, ctx: ExpressionContext<RelationalBinaryOperation>) = AddInstructions {
		appendConditionEvaluation(ctx, "REL" + ctx.original.id) { label ->
			appendStack2Instruction(
				operands, ctx.address(
					when (ctx.original.operator) {
						RelationalBinaryOperator.Equal -> IfIcmpeq(label)
						RelationalBinaryOperator.EqualNot -> IfIcmpne(label)
						RelationalBinaryOperator.GreaterEqual -> IfIcmpge(label)
						RelationalBinaryOperator.Greater -> IfIcmpgt(label)
						RelationalBinaryOperator.SmallerEqual -> IfIcmple(label)
						RelationalBinaryOperator.Smaller -> IfIcmplt(label)
					}
				)
			)
		}
	}

	override fun visitObjectEqualsBinaryOperation(operands: BinaryOperands<AddInstructions>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>) = AddInstructions {
		appendConditionEvaluation(ctx, "REF" + ctx.original.id) { label ->
			appendStack2Instruction(
				operands,
				ctx.address(
					when (ctx.original.operator) {
						ObjectEqualsBinaryOperator.Equal -> IfAcmpeq(label)
						ObjectEqualsBinaryOperator.EqualNot -> IfAcmpne(label)
					}
				)
			)
		}
	}

	override fun visitTernaryOperation(condition: AddInstructions, operands: BinaryOperands<AddInstructions>, ctx: ExpressionContext<TernaryOperation>) = AddInstructions {
		val endLabel = Label("TER" + ctx.original.id + "END")
		val elseLabel = Label("TER" + ctx.original.id + "ELSE")
		appendStack1Instruction(condition, ctx.address(Ifeq(elseLabel)))
		operands.lhs.addInstructions()
		appendStack0Instruction(ctx.address(Goto(endLabel)))
		appendInstruction(ctx.address(elseLabel))
		appendInstruction(ctx.address(EnteringOtherProductionBranch))
		operands.rhs.addInstructions()
		appendInstruction(ctx.address(endLabel))
	}

	override fun visitCoercionExpression(operand: AddInstructions, ctx: ExpressionContext<CoercionExpression>) = AddInstructions {
		when (ctx.original.expectedType) {
			Type.bool -> appendConditionEvaluation(ctx, "BOOL" + ctx.original.id) { appendStack1Instruction(operand, ctx.address(Ifne(it))) }
			Type.int -> operand.addInstructions()
			else -> throw UnsupportedOperationException(ctx.original.expectedType.id)
		}
	}

	override fun visitBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>) = AddInstructions {
		appendStack0Instruction(ctx.address(LdcInt(if (ctx.original.value) 1 else 0)))
	}

	override fun visitIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>) = AddInstructions {
		appendStack0Instruction(ctx.address(LdcInt(ctx.original.value)))
	}

	override fun visitStringLiteral(ctx: ExpressionContext<StringLiteral>) = AddInstructions {
		appendStack0Instruction(ctx.address(LdcString(ctx.original.value)))
	}

	override fun visitNixLiteral(ctx: ExpressionContext<NixLiteral>) = AddInstructions {
		appendStack0Instruction(ctx.address(AconstNull))
	}

	override fun visitThisExpression(ctx: ExpressionContext<ThisExpression>) = AddInstructions {
		appendStack0Instruction(ctx.address(Aload0))
	}

	override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>) = AddInstructions {
		appendStack0Instruction(ctx.address(Load(JasminVariable(ctx.original.variable))))
	}

	override fun visitMemberAccess(operand: AddInstructions, ctx: ExpressionContext<MemberAccess>) = AddInstructions {
		appendStack1Instruction(operand, ctx.address(Getfield(ctx.original.clazz.id, ctx.original.member.id, JasminValueType.create(ctx.original.member.type))))
	}

	override fun visitBuiltinMethodInvocation(arguments: List<AddInstructions>, ctx: ExpressionContext<BuiltinMethodInvocation>) = AddInstructions {
		when (ctx.original.method) {
			is BuiltinPrintMethod -> {
				appendBuiltinPrintMethodInvocation(ctx, ctx.original, arguments)
				appendStack0Instruction(ctx.address(LdcInt(0)))
			}
			ReadIntMethod -> {
				appendStack0Instruction(ctx.address(Getstatic("Main/scanner Ljava/util/Scanner;")))
				appendInstruction(ctx.address(Invokevirtual("java/util/Scanner", JasminSignature("nextInt", listOf(), JasminInt))))
			}
			ReadStringMethod -> {
				appendStack0Instruction(ctx.address(Getstatic("Main/scanner Ljava/util/Scanner;")))
				appendInstruction(ctx.address(Invokevirtual("java/util/Scanner", JasminSignature("nextLine", listOf(), JasminReference.string))))
			}
		}
	}

	override fun visitMethodInvocation(operand: AddInstructions, arguments: List<AddInstructions>, ctx: ExpressionContext<MethodInvocation>) = AddInstructions {
		operand.addInstructions()
		appendExpressions(arguments)
		appendInstruction(ctx.address(Invokevirtual(ctx.original.clazz.id, JasminSignature(ctx.original.methodSignature))))
	}

	override fun visitObjectAllocation(arguments: List<AddInstructions>, ctx: ExpressionContext<ObjectAllocation>) = AddInstructions {
		appendStack0Instruction(ctx.address(New(ctx.original.type.id)))
		appendInstruction(ctx.address(Dup))
		appendExpressions(arguments)
		appendInstruction(ctx.address(Invokespecial(ctx.original.type.id, JasminSignature.mapArgumentTypes(ctx.original.constructorSignature.signature.argumentTypes))))
	}

	private fun appendConditionEvaluation(ctx: ExpressionContext<Expression>, labelId: String, createStackInstruction: (Label) -> Unit) {
		val thenLabel = Label(labelId + "THEN")
		createStackInstruction(thenLabel)
		appendLoadBoolean(ctx, thenLabel, Label(labelId + "END"))
	}

	private fun appendLoadBoolean(ctx: ExpressionContext<Expression>, trueLabel: Label, endLabel: Label) {
		appendStack0Instruction(ctx.address(LdcInt(0)))
		appendStack0Instruction(ctx.address(Goto(endLabel)))
		appendInstruction(ctx.address(trueLabel))
		appendInstruction(ctx.address(EnteringOtherProductionBranch))
		appendStack0Instruction(ctx.address(LdcInt(1)))
		appendInstruction(ctx.address(endLabel))
	}

	private fun appendBuiltinPrintMethodInvocation(ctx: ExpressionContext<Expression>, original: BuiltinMethodInvocation, arguments: List<AddInstructions>) {
		appendStack0Instruction(ctx.address(Getstatic("java/lang/System/out Ljava/io/PrintStream;")))
		appendExpressions(arguments)
		appendInstruction(
			ctx.address(
				Invokevirtual(
					"java/io/PrintStream",
					JasminSignature("print", JasminSignature.mapArgumentTypes(original.method.signature.argumentTypes), JasminVoid)
				)
			)
		)
	}

	private fun appendExpressions(expressions: List<AddInstructions>) {
		for (expression in expressions) {
			expression.addInstructions()
		}
	}
}