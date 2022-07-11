package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.*
import com.franzmandl.compiler.common.TypeError.*
import com.franzmandl.compiler.common.TypeWarning.*
import com.franzmandl.compiler.generated.JovaParser.*
import com.franzmandl.compiler.misc.PhaseMessages

class ScopeVisitor(
	private val clazz: ClassSymbolTable,
	private val errors: PhaseMessages<TypeError>,
	private val bodyInfo: BodyInfo,
	private val level: Int,
	private val needsScanner: () -> Unit,
	private val program: ProgramSymbolTable,
	private val scope: ScopeSymbolTable,
	private val signature: Signature,
	private val warnings: PhaseMessages<TypeWarning>,
) {
	fun visit(
		variablesCtx: List<VariableContext>,
		statementsCtx: List<StatementContext>,
		returnCtx: Pair<ReturnStatementContext, Type>?,
	): Compound {
		val compoundStatements = mutableListOf<CompoundStatement>()
		BasicBlockBuilder().use { basicBlockBuilder ->
			fun visitBasicStatement(ctx: BasicStatementContext) {
				when {
					ctx.assignment() != null -> basicBlockBuilder.addStatement(visitAssignmentStatement(ctx.assignment(), basicBlockBuilder))
					ctx.expression() != null -> basicBlockBuilder.addStatement(ExpressionStatement(visitExpression(ctx.expression(), basicBlockBuilder)))
				}
			}

			fun visitControlStatement(ctx: ControlStatementContext) {
				compoundStatements.add(when {
					ctx.ifStatement() != null -> {
						// Fetch id before visiting any child.
						val id = bodyInfo.incrementBasicBlock()
						val thenBranch = visitCompound(ctx.ifStatement().thenBranch)
						val elseBranch = ctx.ifStatement().elseBranch?.let { visitCompound(it) }
						// Visit thenBranch and elseBranch before condition.
						val condition = visitCondition(ctx.ifStatement().condition, basicBlockBuilder, Util.createLocation(ctx.ifStatement().KEY_IF()))
						IfStatement(basicBlockBuilder.toExpressionBlock(id, condition), thenBranch, elseBranch)
					}
					ctx.whileStatement() != null -> {
						// Would waste IDs without callback.
						basicBlockBuilder.toBasicBlock({ bodyInfo.incrementBasicBlock() }, compoundStatements::add)
						// Fetch id before visiting any child.
						val id = bodyInfo.incrementBasicBlock()
						val compound = visitCompound(ctx.whileStatement().compound())
						// Visit compound before condition.
						for (basicStatementCtx in ctx.whileStatement().basicStatement()) {
							visitBasicStatement(basicStatementCtx)
						}
						val condition = visitCondition(ctx.whileStatement().condition, basicBlockBuilder, Util.createLocation(ctx.whileStatement().KEY_WHILE()))
						WhileStatement(basicBlockBuilder.toExpressionBlock(id, condition), compound)
					}
					else -> throw UnsupportedOperationException(ctx.text)
				}
				)
			}

			for (ctx in variablesCtx) {
				val type = program.getType(ctx.type(), errors)
				if (type != null) {
					val variables = ctx.idList().ids.map { id ->
						bodyInfo.updateTemporaryVariableCounter(id.text)
						scope.createAndAddVariable(id.text, Util.createLocation(id), level, type, signature, errors)
					}
					if (variables.isNotEmpty()) {
						basicBlockBuilder.addStatement(VariableDeclarations(type, variables))
					}
				}
			}
			for (ctx in statementsCtx) {
				when {
					ctx.basicStatement() != null -> handleStatementException { visitBasicStatement(ctx.basicStatement()) }
					ctx.controlStatement() != null -> handleStatementException { visitControlStatement(ctx.controlStatement()) }
					else -> throw UnsupportedOperationException(ctx.text)
				}
			}
			val returnExpression = returnCtx?.let { (ctx, returnType) -> handleStatementException { visitReturn(ctx, returnType, basicBlockBuilder) } }
			if (returnExpression != null) {
				compoundStatements.add(ReturnStatement(basicBlockBuilder.toExpressionBlock(bodyInfo.incrementBasicBlock(), returnExpression)))
			} else {
				basicBlockBuilder.toBasicBlock({ bodyInfo.incrementBasicBlock() }, compoundStatements::add)
				null
			}
		}
		return Compound(compoundStatements)
	}

	private fun visitCondition(ctx: ExpressionContext, basicBlockBuilder: BasicBlockBuilder, operatorLocation: Location): Expression {
		val uncheckedCondition = visitExpression(ctx, basicBlockBuilder)
		fun createTypeError() = IncompatibleConditionTypeError(operatorLocation, Type.toString(uncheckedCondition.type))
		fun createCoercionWarning(resultType: String) = ConditionCoercionWarning(operatorLocation, Type.toString(uncheckedCondition.type), resultType)
		return checkUnaryExpression(Type.bool, Type.int, uncheckedCondition, ::createTypeError, ::createCoercionWarning)
	}

	private fun visitCompound(ctx: CompoundContext) =
		ScopeVisitor(clazz, errors, bodyInfo, level + 1, needsScanner, program, ScopeSymbolTable(scope), signature, warnings).visit(listOf(), ctx.statement(), null)

	private fun visitAssignableExpression(ctx: AssignableExpressionContext, basicBlockBuilder: BasicBlockBuilder, type: Type?): AssignableExpression {
		val expression = if (ctx.KEY_THIS() != null) {
			ThisExpression(clazz.type, true)
		} else {
			val id = ctx.ID()?.text ?: throw UnsupportedOperationException(ctx.text)
			scope.getVariable(id)?.let { VariableAccess(it) }
				?: clazz.getMember(id)?.let { MemberAccess(ThisExpression(clazz.type, false), clazz.type, it) }
				?: if (bodyInfo.updateTemporaryVariableCounter(id)) {  // id matches temporary variable pattern
					if (type != null && ctx.chainedId().isEmpty()) {  // if type != null, then expression is the lhs of an assignment
						val temporaryVariable = Variable(id, null, type)
						if (!basicBlockBuilder.variables.add(temporaryVariable)) {
							errors.add(VariableDoubleDefinitionTypeError(Util.createLocation(ctx.ID()), id, type.id, signature))
						}
						VariableAccess(temporaryVariable)
					} else {
						basicBlockBuilder.variables[id]?.let { VariableAccess(it) }
					}
				} else {
					null
				}
				?: throw StatementException(UndefinedIdError(Util.createLocation(ctx), id))
		}
		return ctx.chainedId().fold(expression) { currentExpression, chainedIdCtx ->
			val id = chainedIdCtx.ID().text
			val expressionClass = program.getClass(currentExpression.type) ?: throw StatementException(DoesNotHaveMemberError(Util.createLocation(chainedIdCtx), currentExpression.type.id, id))
			val member = expressionClass.getMember(id) ?: throw StatementException(DoesNotHaveMemberError(Util.createLocation(chainedIdCtx), expressionClass.id, id))
			if (member.accessModifier == AccessModifier.Public || expressionClass == clazz) {
				MemberAccess(currentExpression, expressionClass.type, member)
			} else {
				throw StatementException(MemberAccessError(Util.createLocation(chainedIdCtx), id, expressionClass.id))
			}
		}
	}

	private fun visitAssignmentStatement(ctx: AssignmentContext, basicBlockBuilder: BasicBlockBuilder): Assignment {
		val uncheckedRhsExpression = visitExpression(ctx.expression(), basicBlockBuilder)
		val lhsExpression = visitAssignableExpression(ctx.assignableExpression(), basicBlockBuilder, uncheckedRhsExpression.type ?: Type.langObject)

		fun createTypeError() = BinaryTypeError(Util.createLocation(ctx.ASSIGN()), lhsExpression.type.id, Type.toString(uncheckedRhsExpression.type), ctx.ASSIGN().text)
		fun createCoercionWarning(resultType: String) =
			BinaryCoercionWarning(Util.createLocation(ctx.ASSIGN()), ctx.ASSIGN().text, lhsExpression.type.id, Type.toString(uncheckedRhsExpression.type), resultType, resultType)

		return Assignment(lhsExpression, checkExpression(lhsExpression.type, uncheckedRhsExpression, ::createTypeError, ::createCoercionWarning))
	}

	private fun visitObjectAllocation(ctx: ObjectAllocationContext, basicBlockBuilder: BasicBlockBuilder): ObjectAllocation {
		val type = Type(ctx.CLASS_TYPE().text)
		if (type.isMain) {
			throw StatementException(MainInstantiationError(Util.createLocation(ctx)))
		}
		val expressionClass = program.getClass(type) ?: throw StatementException(UnknownTypeError(Util.createLocation(ctx), type.id))
		return if (ctx.constructorArguments() == null) {
			val signature = Signature(expressionClass.id, listOf())
			val constructorSignature = ConstructorSignature(true, signature)
			if (expressionClass.getConstructorSignature(SignatureNullable(signature)) == null) {
				expressionClass.addConstructorSignature(constructorSignature)
				expressionClass.addConstructor(Constructor(constructorSignature, Body.emptyBody))
			}
			ObjectAllocation(expressionClass.type, constructorSignature, listOf())
		} else {
			val arguments = visitArguments(ctx.constructorArguments().argumentList(), basicBlockBuilder)
			val signatureNullable = SignatureNullable(expressionClass.id, arguments.map { it.type })
			val constructorSignature = expressionClass.getConstructorSignature(signatureNullable)
				?: throw StatementException(UndefinedMethodError(Util.createLocation(ctx), signatureNullable))
			ObjectAllocation(expressionClass.type, constructorSignature, arguments)
		}
	}

	private fun visitArguments(ctx: ArgumentListContext?, basicBlockBuilder: BasicBlockBuilder): List<Expression> =
		ctx?.arguments?.map { visitExpression(it, basicBlockBuilder) } ?: listOf()

	private fun visitPrimaryExpression(ctx: PrimaryExpressionContext, basicBlockBuilder: BasicBlockBuilder): Expression =
		when {
			ctx.literal() != null -> visitLiteral(ctx.literal())
			ctx.idExpression() != null -> visitIdExpression(ctx.idExpression(), basicBlockBuilder)
			ctx.methodInvocation() != null -> visitInvocation(null, ctx.methodInvocation(), basicBlockBuilder)
			ctx.parenthesisExpression() != null -> visitExpression(ctx.parenthesisExpression().expression(), basicBlockBuilder)
			ctx.unaryExpression() != null -> visitUnaryExpression(ctx.unaryExpression(), basicBlockBuilder)
			else -> throw UnsupportedOperationException(ctx.text)
		}

	private fun visitLiteral(ctx: LiteralContext): LiteralExpression =
		when {
			ctx.BOOL_LIT() != null -> when (ctx.BOOL_LIT().text) {
				"false" -> BooleanLiteralFalse
				"true" -> BooleanLiteralTrue
				else -> throw UnsupportedOperationException(ctx.text)
			}
			ctx.INT_LIT() != null -> IntegerLiteral(ctx.INT_LIT().text.toInt())
			ctx.STRING_LIT() != null -> StringLiteral(ctx.STRING_LIT().text)
			ctx.KEY_NIX() != null -> NixLiteral
			else -> throw UnsupportedOperationException(ctx.text)
		}

	private fun visitIdExpression(ctx: IdExpressionContext, basicBlockBuilder: BasicBlockBuilder): Expression {
		val expression = visitAssignableExpression(ctx.assignableExpression(), basicBlockBuilder, null)
		return ctx.chainedMethodInvocation()?.let { visitInvocation(expression, it.methodInvocation(), basicBlockBuilder) } ?: expression
	}

	private fun visitInvocation(expression: Expression?, ctx: MethodInvocationContext, basicBlockBuilder: BasicBlockBuilder): InvocationExpression {
		val arguments = visitArguments(ctx.argumentList(), basicBlockBuilder)
		val signatureNullable = SignatureNullable(ctx.ID().text, arguments.map { it.type })
		return if (expression != null) {
			val expressionClass = program.getClass(expression.type)
				?: throw StatementException(CannotInvokeError(Util.createLocation(ctx), Type.toString(expression.type), signatureNullable))
			val methodSignature = expressionClass.getMethodSignature(signatureNullable)
				?: throw StatementException(CannotInvokeError(Util.createLocation(ctx), Type.toString(expression.type), signatureNullable))
			if (methodSignature.accessModifier == AccessModifier.Public || expressionClass == clazz) {
				MethodInvocation(expression, expressionClass.type, methodSignature, arguments)
			} else {
				throw StatementException(MethodAccessError(Util.createLocation(ctx), methodSignature.signature, expressionClass.id))
			}
		} else {
			val methodSignature = clazz.getMethodSignature(signatureNullable)
			if (methodSignature != null) {
				MethodInvocation(ThisExpression(clazz.type, false), clazz.type, methodSignature, arguments)
			} else {
				Util.builtinMethods[signatureNullable]?.let {
					if (it is BuiltinReadMethod) {
						needsScanner()
					}
					BuiltinMethodInvocation(it, arguments)
				} ?: throw StatementException(UndefinedMethodError(Util.createLocation(ctx), signatureNullable))
			}
		}
	}

	private fun visitExpression(ctx: ExpressionContext, basicBlockBuilder: BasicBlockBuilder): Expression {
		if (ctx.primaryExpression() != null) {
			return visitPrimaryExpression(ctx.primaryExpression(), basicBlockBuilder)
		}
		if (ctx.objectAllocation() != null) {
			return visitObjectAllocation(ctx.objectAllocation(), basicBlockBuilder)
		}
		val conditionExpression = ctx.condition?.let { visitCondition(it, basicBlockBuilder, Util.createLocation(ctx.conditionOperator)) }
		val uncheckedLhsExpression = visitExpression(ctx.lhs, basicBlockBuilder)
		val uncheckedRhsExpression = visitExpression(ctx.rhs, basicBlockBuilder)
		val operator = ctx.operator ?: throw UnsupportedOperationException(ctx.text)

		fun areReferences(): Boolean {
			val lhsExpressionClassExists = program.hasClass(uncheckedLhsExpression.type)
			val rhsExpressionClassExists = program.hasClass(uncheckedRhsExpression.type)
			fun isClassClass() = lhsExpressionClassExists && uncheckedLhsExpression.type == uncheckedRhsExpression.type
			fun isClassNix() = lhsExpressionClassExists && uncheckedRhsExpression.type == null
			fun isNixClass() = uncheckedLhsExpression.type == null && rhsExpressionClassExists
			fun isNixNix() = uncheckedLhsExpression.type == null && uncheckedRhsExpression.type == null
			return isClassClass() || isClassNix() || isNixClass() || isNixNix()
		}

		fun createTypeError() = BinaryTypeError(Util.createLocation(operator), Type.toString(uncheckedLhsExpression.type), Type.toString(uncheckedRhsExpression.type), operator.text)
		fun createCoercionWarning(resultType: String) =
			BinaryCoercionWarning(Util.createLocation(operator), operator.text, Type.toString(uncheckedLhsExpression.type), Type.toString(uncheckedRhsExpression.type), resultType, resultType)

		if (conditionExpression != null) {
			val rhsExpression = if (areReferences()) {
				uncheckedRhsExpression
			} else {
				checkExpression(uncheckedLhsExpression.type, uncheckedRhsExpression, ::createTypeError, ::createCoercionWarning)
			}
			return TernaryOperation(bodyInfo.incrementTernary(), conditionExpression, BinaryOperands(uncheckedLhsExpression, rhsExpression))
		} else {
			ObjectEqualsBinaryOperator.operators[operator.text]?.let {
				if (areReferences()) {
					return ObjectEqualsBinaryOperation(bodyInfo.incrementObjectEquals(), it, BinaryOperands(uncheckedLhsExpression, uncheckedRhsExpression))
				}
			}
			ArithmeticBinaryOperator.operators[operator.text]?.let {
				return ArithmeticBinaryOperation(it, checkBinaryExpression(Type.int, Type.bool, uncheckedLhsExpression, uncheckedRhsExpression, ::createTypeError, ::createCoercionWarning))
			}
			RelationalBinaryOperator.operators[operator.text]?.let {
				return RelationalBinaryOperation(
					bodyInfo.incrementRelational(),
					it,
					checkBinaryExpression(Type.int, Type.bool, uncheckedLhsExpression, uncheckedRhsExpression, ::createTypeError, ::createCoercionWarning)
				)
			}
			LogicalBinaryOperator.operators[operator.text]?.let {
				return LogicalBinaryOperation(
					bodyInfo.incrementLogical(it),
					it,
					checkBinaryExpression(Type.bool, Type.int, uncheckedLhsExpression, uncheckedRhsExpression, ::createTypeError, ::createCoercionWarning)
				)
			}
			throw UnsupportedOperationException(ctx.text)
		}
	}

	private fun visitUnaryExpression(ctx: UnaryExpressionContext, basicBlockBuilder: BasicBlockBuilder): Expression {
		val uncheckedPrimaryExpression = visitPrimaryExpression(ctx.primaryExpression(), basicBlockBuilder)
		fun createTypeError() = UnaryTypeError(Util.createLocation(ctx.op), Type.toString(uncheckedPrimaryExpression.type), ctx.op.text)
		fun createCoercionWarning(resultType: String) = UnaryCoercionWarning(Util.createLocation(ctx.op), ctx.op.text, Type.toString(uncheckedPrimaryExpression.type), resultType)
		ArithmeticUnaryOperator.operators[ctx.op.text]?.let {
			return if (it == ArithmeticUnaryOperator.Minus && uncheckedPrimaryExpression is IntegerLiteral && uncheckedPrimaryExpression.value > 0) {
				IntegerLiteral(-uncheckedPrimaryExpression.value)
			} else {
				ArithmeticUnaryOperation(it, checkUnaryExpression(Type.int, Type.bool, uncheckedPrimaryExpression, ::createTypeError, ::createCoercionWarning))
			}
		}
		if (ctx.op.text == LogicalUnaryOperator.notSign) {
			return LogicalNotUnaryOperation(bodyInfo.incrementLogicalNot(), checkUnaryExpression(Type.bool, Type.int, uncheckedPrimaryExpression, ::createTypeError, ::createCoercionWarning))
		}
		throw UnsupportedOperationException(ctx.text)
	}

	private fun visitReturn(ctx: ReturnStatementContext, returnType: Type, basicBlockBuilder: BasicBlockBuilder): Expression {
		val uncheckedExpression = visitExpression(ctx.expression(), basicBlockBuilder)
		fun createTypeError() = IncompatibleReturnTypeError(Util.createLocation(ctx.KEY_RETURN()), Type.toString(uncheckedExpression.type))
		fun createCoercionWarning(resultType: String) = ReturnCoercionWarning(Util.createLocation(ctx.KEY_RETURN()), Type.toString(uncheckedExpression.type), resultType)
		return checkExpression(returnType, uncheckedExpression, ::createTypeError, ::createCoercionWarning)
	}

	private fun checkExpression(
		expectedType: Type?,
		expression: Expression,
		createTypeError: () -> TypeError,
		createCoercionWarning: (String) -> TypeWarning
	): Expression =
		when (expectedType) {
			Type.bool -> checkUnaryExpression(expectedType, Type.int, expression, createTypeError, createCoercionWarning)
			Type.int -> checkUnaryExpression(expectedType, Type.bool, expression, createTypeError, createCoercionWarning)
			expression.type -> expression
			else -> if (program.hasClass(expectedType) && expression.type == null) {
				expression
			} else {
				throw StatementException(createTypeError())
			}
		}

	private fun checkUnaryExpression(
		expectedType: Type,
		acceptedType: Type,
		expression: Expression,
		createTypeError: () -> TypeError,
		createCoercionWarning: (String) -> TypeWarning
	): Expression =
		when (expression.type) {
			acceptedType -> coerceWithWarning(expectedType, acceptedType, expression, createCoercionWarning)
			expectedType -> expression
			else -> throw StatementException(createTypeError())
		}

	private fun checkBinaryExpression(
		expectedType: Type,
		acceptedType: Type,
		lhsExpression: Expression,
		rhsExpression: Expression,
		createTypeError: () -> TypeError,
		createCoercionWarning: (String) -> TypeWarning,
	): BinaryOperands<Expression> = when (lhsExpression.type to rhsExpression.type) {
		expectedType to expectedType ->
			BinaryOperands(lhsExpression, rhsExpression)
		expectedType to acceptedType ->
			BinaryOperands(lhsExpression, coerceWithWarning(expectedType, acceptedType, rhsExpression, createCoercionWarning))
		acceptedType to expectedType ->
			BinaryOperands(coerceWithWarning(expectedType, acceptedType, lhsExpression, createCoercionWarning), rhsExpression)
		acceptedType to acceptedType ->
			BinaryOperands(
				coerceWithWarning(expectedType, acceptedType, lhsExpression, createCoercionWarning),
				CoercionExpression(bodyInfo.incrementCoercion(expectedType), rhsExpression, expectedType, acceptedType)
			)
		else -> throw StatementException(createTypeError())
	}

	private fun coerceWithWarning(
		expectedType: Type,
		acceptedType: Type,
		expression: Expression,
		createCoercionWarning: (String) -> TypeWarning,
	): Expression {
		warnings.add(createCoercionWarning(expectedType.id))
		return CoercionExpression(bodyInfo.incrementCoercion(expectedType), expression, expectedType, acceptedType)
	}

	private class StatementException(val error: TypeError) : Throwable()

	private fun <T> handleStatementException(callback: () -> T): T? =
		try {
			callback()
		} catch (statementException: StatementException) {
			errors.add(statementException.error)
			null
		}
}