package com.franzmandl.compiler.code.jova

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*

object JovaFormatter {
	private const val indent = "    "

	private fun br(level: Int) = "\n" + indent.repeat(level)

	private fun <T> joinToStringIndexed(iterable: Iterable<T>, separator: String, transform: (Int, T) -> CharSequence): String {
		val builder = StringBuilder()
		var first = true
		for ((index, item) in iterable.withIndex()) {
			if (first) {
				first = false
			} else {
				builder.append(separator)
			}
			builder.append(transform(index, item))
		}
		return builder.toString()
	}

	fun format(program: Program, address: Address): String {
		var result = ""
		when (address) {
			is ProgramAddress -> {
				result = formatProgram(program)
			}
			is BodyAddress -> ProgramContext(program).mapBody(address) {
				result = formatBody(it)
				it.original
			}
			is CompoundAddress -> ProgramContext(program).mapCompound(address) {
				result = formatCompound(0, it)
				it.original
			}
			is BasicBlockAddress -> ProgramContext(program).mapBasicBlock(address) {
				result = formatBasicBlock(0, it)
				it.original
			}
			is ExpressionBlockAddress -> ProgramContext(program).mapExpressionBlock(address) {
				result = formatExpressionBlock(0, it)
				it.original
			}
			else -> throw IllegalStateException("Unsupported address type.")
		}
		return result
	}

	private fun formatProgram(program: Program) =
		program.classes.joinToString("\n\n") { clazz ->
			"class " + clazz.id + " {" + clazz.symbols.joinToString("") { formatSymbol(clazz.id, it) } + br(0) + "}"
		}

	private fun formatSymbol(classId: String, symbol: ClassSymbol) =
		when (symbol) {
			is Constructor -> if (symbol.constructorSignature.isDefault) {
				""
			} else {
				formatBody(BodyContext(BodyAddress(classId, symbol.constructorSignature.signature), symbol))
			}
			is Member -> br(1) + symbol.accessModifier.modifier + " " + symbol.type.id + " " + symbol.id + ";"
			is Method -> formatBody(BodyContext(BodyAddress(classId, symbol.methodSignature.signature), symbol))
		}

	private fun formatBody(ctx: BodyContext): String =
		when (ctx.symbol) {
			is Constructor -> br(0) + br(1) +
					ctx.symbol.constructorSignature.signature.name + "(" +
					formatArguments(ctx.symbol.body.arguments) + ") {" +
					formatCompound(2, ctx.compound) + br(1) + "}"
			is Method -> br(0) + br(1) +
					ctx.symbol.methodSignature.accessModifier.modifier + " " +
					ctx.symbol.methodSignature.returnType.id + " " +
					ctx.symbol.methodSignature.signature.name + "(" +
					formatArguments(ctx.symbol.body.arguments) + ") {" +
					formatCompound(2, ctx.compound) + br(1) + "}"

		}

	private fun formatArguments(variables: List<Variable>) = variables.joinToString(", ") { it.type.id + " " + it.id }

	private fun formatCompound(level: Int, ctx: CompoundContext) =
		ctx.original.statements.joinToString("") { formatCompoundStatement(level, it, ctx) }

	private fun formatCompoundStatement(level: Int, statement: CompoundStatement, ctx: CompoundContext): String =
		when (statement) {
			is BasicBlock -> formatBasicBlock(level, ctx.enterBasicBlock(statement))
			is ControlStatement -> formatExpressionBlock(level, ctx.enterExpressionBlock(statement)) + when (statement) {
				is IfStatement -> " {" +
						formatCompound(level + 1, ctx.enterThenBranch(statement)) + br(level) + "}" +
						(ctx.enterElseBranch(statement)?.let { " else {" + formatCompound(level + 1, it) + br(level) + "}" } ?: "")
				is ReturnStatement -> ""
				is WhileStatement -> " {" +
						formatCompound(level + 1, ctx.enterWhileBranch(statement)) + br(level) + "}"
			}

		}

	private fun formatExpressionBlock(level: Int, ctx: ExpressionBlockContext): String =
		when (ctx.statement) {
			is IfStatement -> formatBasicBlock(level, ctx.basicBlock) + br(level) + "if (" + ctx.expression.visitExpression(Visitor) + ")"
			is ReturnStatement -> formatBasicBlock(level, ctx.basicBlock) + br(level) + "return " + ctx.expression.visitExpression(Visitor) + ";"
			is WhileStatement -> br(level) + "while (" + joinToStringIndexed(ctx.basicBlock.original.statements, "") { index, statement ->
				formatBasicStatement(statement, ctx.basicBlock, index) + "; "
			} + ctx.expression.visitExpression(Visitor) + ")"
		}

	private fun formatBasicBlock(level: Int, ctx: BasicBlockContext): String =
		joinToStringIndexed(ctx.original.statements, "") { index, statement -> br(level) + formatBasicStatement(statement, ctx, index) + ";" }

	private fun formatBasicStatement(statement: BasicStatement, ctx: BasicBlockContext, index: Int): String =
		when (statement) {
			is Assignment -> ctx.enterAssignmentLhs(statement, index).visitExpression(Visitor) + " = " + ctx.enterAssignmentRhs(statement, index).visitExpression(Visitor)
			is ExpressionStatement -> ctx.enterExpressionStatementExpression(statement, index).visitExpression(Visitor)
			is VariableDeclarations -> statement.type.id + " " + statement.variables.joinToString(", ") { it.id }
		}

	private object Visitor : ExpressionVisitor<String> {
		private fun wrapBraces(needsBraces: Boolean, operand: String) =
			if (needsBraces) "($operand)" else operand

		override fun visitArithmeticUnaryOperation(operand: String, ctx: ExpressionContext<ArithmeticUnaryOperation>) =
			ctx.original.operator.sign + wrapBraces(ctx.original.needsBraces, operand)

		override fun visitLogicalNotUnaryOperation(operand: String, ctx: ExpressionContext<LogicalNotUnaryOperation>) =
			"!" + wrapBraces(ctx.original.needsBraces, operand)

		override fun visitArithmeticBinaryOperation(operands: BinaryOperands<String>, ctx: ExpressionContext<ArithmeticBinaryOperation>) = visitBinaryOperation(operands, ctx.original)
		override fun visitLogicalBinaryOperation(operands: BinaryOperands<String>, ctx: ExpressionContext<LogicalBinaryOperation>) = visitBinaryOperation(operands, ctx.original)
		override fun visitRelationalBinaryOperation(operands: BinaryOperands<String>, ctx: ExpressionContext<RelationalBinaryOperation>) = visitBinaryOperation(operands, ctx.original)
		override fun visitObjectEqualsBinaryOperation(operands: BinaryOperands<String>, ctx: ExpressionContext<ObjectEqualsBinaryOperation>) = visitBinaryOperation(operands, ctx.original)

		private fun visitBinaryOperation(operands: BinaryOperands<String>, original: BinaryOperation) =
			wrapBraces(original.needsLhsBraces, operands.lhs) + " " + original.operator.sign + " " + wrapBraces(original.needsRhsBraces, operands.rhs)

		override fun visitTernaryOperation(condition: String, operands: BinaryOperands<String>, ctx: ExpressionContext<TernaryOperation>) =
			wrapBraces(ctx.original.needsConditionBraces, condition) +
					" ? " + wrapBraces(ctx.original.needsLhsBraces, operands.lhs) +
					" : " + wrapBraces(ctx.original.needsRhsBraces, operands.rhs)

		override fun visitCoercionExpression(operand: String, ctx: ExpressionContext<CoercionExpression>) = operand
		override fun visitBooleanLiteral(ctx: ExpressionContext<BooleanLiteral>) = if (ctx.original.value) "true" else "false"
		override fun visitIntegerLiteral(ctx: ExpressionContext<IntegerLiteral>) = ctx.original.value.toString()
		override fun visitStringLiteral(ctx: ExpressionContext<StringLiteral>) = ctx.original.value
		override fun visitNixLiteral(ctx: ExpressionContext<NixLiteral>) = "nix"
		override fun visitThisExpression(ctx: ExpressionContext<ThisExpression>) = if (ctx.original.isExplicitly) "this" else ""
		override fun visitVariableAccess(ctx: ExpressionContext<VariableAccess>) = ctx.original.variable.id
		override fun visitMemberAccess(operand: String, ctx: ExpressionContext<MemberAccess>) = (if (operand.isEmpty()) "" else "$operand.") + ctx.original.member.id

		private fun visitArguments(arguments: List<String>) = "(" + arguments.joinToString(", ") + ")"
		override fun visitBuiltinMethodInvocation(arguments: List<String>, ctx: ExpressionContext<BuiltinMethodInvocation>) = ctx.original.method.signature.name + visitArguments(arguments)

		override fun visitMethodInvocation(operand: String, arguments: List<String>, ctx: ExpressionContext<MethodInvocation>) =
			(if (operand.isEmpty()) "" else "$operand.") + ctx.original.methodSignature.signature.name + visitArguments(arguments)

		override fun visitObjectAllocation(arguments: List<String>, ctx: ExpressionContext<ObjectAllocation>) =
			"new " + ctx.original.type.id + (if (ctx.original.constructorSignature.isDefault) "" else visitArguments(arguments))
	}
}