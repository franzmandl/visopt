package com.franzmandl.compiler.server

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.*
import com.franzmandl.compiler.optimizer.ConstantFolding
import com.franzmandl.compiler.suite.Util.createCompound
import com.franzmandl.compiler.suite.Util.createExpressionAddress
import com.franzmandl.compiler.suite.Util.createExpressionBlock
import com.franzmandl.compiler.suite.Util.createPrintStringMethod
import com.franzmandl.compiler.suite.Util.createProgram
import com.franzmandl.compiler.suite.Util.dummyBodyAddress
import com.franzmandl.compiler.suite.Util.dummyOptimization
import com.franzmandl.compiler.suite.Util.replaceExpression
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

class CommandTests {
	private val plusOperation = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Plus, BinaryOperands(IntegerLiteral(100), IntegerLiteral(200)))
	private val plusResult = IntegerLiteral(300)
	private val minusOperation = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Minus, BinaryOperands(IntegerLiteral(300), IntegerLiteral(400)))
	private val minusResult = IntegerLiteral(-100)
	private val retVariable = Variable("ret", 0, Type.int)
	private val retVariableDeclaration = VariableDeclarations(retVariable.type, listOf(retVariable))

	@Test
	fun test1() {
		val program0 = createProgram(
			createExpressionBlock(
				1,
				statements = listOf(
					ExpressionStatement(plusOperation),
					ExpressionStatement(minusOperation),
				), expression = IntegerLiteral.p0
			)
		)
		val program1 = replaceExpression(program0, createExpressionAddress(listOf(), 0, 0, 0, listOf()), plusResult)
		val program2 = replaceExpression(program1, createExpressionAddress(listOf(), 0, 1, 0, listOf()), minusResult)
		Assertions.assertThat(program2).isEqualTo(
			createProgram(
				createExpressionBlock(
					1,
					statements = listOf(
						ExpressionStatement(plusResult),
						ExpressionStatement(minusResult),
					), expression = IntegerLiteral.p0
				)
			)
		)
	}

	@Test
	fun test2() {
		val program0 = createProgram(
			createExpressionBlock(
				1,
				statements = listOf(
					retVariableDeclaration,
					Assignment(VariableAccess(retVariable), BuiltinMethodInvocation(PrintIntMethod, listOf(plusOperation))),
					Assignment(VariableAccess(retVariable), BuiltinMethodInvocation(PrintIntMethod, listOf(minusOperation))),
				), expression = IntegerLiteral.p0
			)
		)
		val program1 = replaceExpression(program0, createExpressionAddress(listOf(), 0, 1, 1, listOf(0)), plusResult)
		val program2 = replaceExpression(program1, createExpressionAddress(listOf(), 0, 2, 1, listOf(0)), minusResult)
		Assertions.assertThat(program2).isEqualTo(
			createProgram(
				createExpressionBlock(
					1,
					statements = listOf(
						retVariableDeclaration,
						Assignment(VariableAccess(retVariable), BuiltinMethodInvocation(PrintIntMethod, listOf(plusResult))),
						Assignment(VariableAccess(retVariable), BuiltinMethodInvocation(PrintIntMethod, listOf(minusResult))),
					), expression = IntegerLiteral.p0
				)
			)
		)
	}

	@Test
	fun test3() {
		val expression00000 = IntegerLiteral(2)
		val expression0000100 = IntegerLiteral(14)
		val expression0000101 = IntegerLiteral(3)
		val expression000010 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression0000100, expression0000101))
		val expression0000110 = IntegerLiteral(73)
		val expression00001110 = IntegerLiteral(64)
		val expression00001111 = IntegerLiteral(8)
		val expression0000111 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression00001110, expression00001111))
		val expression000011 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression0000110, expression0000111))
		val expression00001 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression000010, expression000011))
		val expression0000 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression00000, expression00001))
		val expression0001 = IntegerLiteral(10)
		val expression000 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression0000, expression0001))
		val expression001 = IntegerLiteral(5)
		val expression00 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression000, expression001))
		val expression01 = IntegerLiteral(7)
		val expression0 = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression00, expression01))
		val expression = BuiltinMethodInvocation(PrintIntMethod, listOf(expression0))
		val program0 =
			createProgram(
				createExpressionBlock(1, statements = listOf(retVariableDeclaration, Assignment(VariableAccess(retVariable), expression)), expression = IntegerLiteral.p0)
			)
		val commands = mutableListOf<Command>()
		ProgramContext(program0).visitExpressions(ConstantFolding.createExpressionReplaceVisitor(commands::add))
		Assertions.assertThat(commands).hasSize(8)
	}

	@Test
	fun test4() {
		val expression00 = IntegerLiteral(1)
		val expression0 = BuiltinMethodInvocation(PrintIntMethod, listOf(expression00))
		val expression1 = IntegerLiteral(2)
		val expression = ArithmeticBinaryOperation(ArithmeticBinaryOperator.Star, BinaryOperands(expression0, expression1))
		val program0 = createProgram(
			createExpressionBlock(1, statements = listOf(retVariableDeclaration, Assignment(VariableAccess(retVariable), expression)), expression = IntegerLiteral.p0)
		)
		val commands = mutableListOf<Command>()
		ProgramContext(program0).visitExpressions(ConstantFolding.createExpressionReplaceVisitor(commands::add))
		Assertions.assertThat(commands).hasSize(0)
	}

	@Test
	fun test5() {
		val program0 = createProgram(
			listOf(
				IfStatement(
					createExpressionBlock(1, statements = listOf(retVariableDeclaration, Assignment(VariableAccess(retVariable), plusOperation)), expression = BooleanLiteralTrue),
					createCompound(2, basicStatement = Assignment(VariableAccess(retVariable), plusOperation)),
					null
				),
				ReturnStatement(createExpressionBlock(3, statement = Assignment(VariableAccess(retVariable), minusOperation), expression = IntegerLiteral.p0))
			)
		)
		val commands = mutableListOf<Command>()
		val expectedProgram = ProgramContext(program0).visitExpressions(ConstantFolding.createExpressionReplaceVisitor(commands::add))
		Assertions.assertThat(commands).hasSize(3)
		val program1 = commands[0].apply(program0)
		val program2 = commands[1].apply(program1)
		val program3 = commands[2].apply(program2)
		Assertions.assertThat(program3).isEqualTo(expectedProgram)
	}

	@Test
	fun test6() {
		val statement000 = Assignment(VariableAccess(retVariable), createPrintStringMethod(StringLiteral("true")))
		val statement00 = createCompound(2, basicStatement = statement000)
		val statement010 = Assignment(VariableAccess(retVariable), createPrintStringMethod(StringLiteral("false")))
		val statement01 = createCompound(3, basicStatement = statement010)
		val condition = createExpressionBlock(1, statement = retVariableDeclaration, expression = BooleanLiteralTrue)
		val statement0 = IfStatement(condition, statement00, statement01)
		val program0 = createProgram(listOf(statement0, ReturnStatement(createExpressionBlock(4, expression = IntegerLiteral.p0))))
		val compoundStatementAddress = CompoundStatementAddress(CompoundAddress(dummyBodyAddress, listOf()), 0)
		val programThenBranch = TakeBranch(dummyOptimization, null, compoundStatementAddress, condition.basicBlock, statement00, "").apply(program0)
		val programElseBranch = TakeBranch(dummyOptimization, null, compoundStatementAddress, condition.basicBlock, statement01, "").apply(program0)
		Assertions.assertThat(programThenBranch)
			.isEqualTo(createProgram(createExpressionBlock(1, statements = listOf(retVariableDeclaration, statement000), expression = IntegerLiteral.p0)))
		Assertions.assertThat(programElseBranch)
			.isEqualTo(createProgram(createExpressionBlock(1, statements = listOf(retVariableDeclaration, statement010), expression = IntegerLiteral.p0)))
	}

	@Test
	fun test7() {
		/*
		L1: compoundStatement0basicStatement0
		if(compoundStatement0basicStatement1) {
			L2:
			if(compoundStatement000basicStatement0) {
				L3: compoundStatement00000basicStatement0
			} else {
				L4: compoundStatement00010basicStatement0
			}
		} else {
			L5:
			if(compoundStatement010basicStatement0) {
				L6: compoundStatement01000basicStatement0
			} else {
				L7: compoundStatement01010basicStatement0
			}
		}
		 */
		val compoundStatement0basicStatement0 = retVariableDeclaration
		val compoundStatement00000basicStatement0expression0 = StringLiteral("statement10000")
		val compoundStatement00000basicStatement0 = Assignment(VariableAccess(retVariable), createPrintStringMethod(compoundStatement00000basicStatement0expression0))
		val compoundStatement00010basicStatement0expression0 = StringLiteral("statement10010")
		val compoundStatement00010basicStatement0 = Assignment(VariableAccess(retVariable), createPrintStringMethod(compoundStatement00010basicStatement0expression0))
		val compoundStatement000 =
			IfStatement(
				createExpressionBlock(2, expression = BooleanLiteralTrue),
				createCompound(3, basicStatement = compoundStatement00000basicStatement0),
				createCompound(4, basicStatement = compoundStatement00010basicStatement0)
			)
		val compoundStatement01000basicStatement0expression0 = StringLiteral("compoundStatement01000basicStatement0")
		val compoundStatement01000basicStatement0 = Assignment(VariableAccess(retVariable), createPrintStringMethod(compoundStatement01000basicStatement0expression0))
		val compoundStatement01010basicStatement0expression0 = StringLiteral("compoundStatement01010basicStatement0")
		val compoundStatement01010basicStatement0 = Assignment(VariableAccess(retVariable), createPrintStringMethod(compoundStatement01010basicStatement0expression0))
		val compoundStatement010 =
			IfStatement(
				createExpressionBlock(5, expression = BooleanLiteralTrue),
				createCompound(6, basicStatement = compoundStatement01000basicStatement0),
				createCompound(7, basicStatement = compoundStatement01010basicStatement0)
			)
		val compoundStatement0 =
			IfStatement(
				createExpressionBlock(1, statement = compoundStatement0basicStatement0, expression = BooleanLiteralTrue),
				createCompound(statement = compoundStatement000),
				createCompound(statement = compoundStatement010)
			)
		val program0 = createProgram(listOf(compoundStatement0, ReturnStatement(createExpressionBlock(8, expression = IntegerLiteral.p0))))
		val program1 = replaceExpression(program0, createExpressionAddress(listOf(), 0, 1, 0, listOf()), BooleanLiteralFalse)
		val program2 = replaceExpression(program1, createExpressionAddress(listOf(0, 0), 0, 0, 0, listOf()), BooleanLiteralFalse)
		val program3 = replaceExpression(program2, createExpressionAddress(listOf(0, 1), 0, 0, 0, listOf()), BooleanLiteralFalse)
		val replacement = StringLiteral("replaced")
		val program4 = replaceExpression(program3, createExpressionAddress(listOf(0, 0, 0, 0), 0, 0, 1, listOf(0)), replacement)
		val program5 = replaceExpression(program4, createExpressionAddress(listOf(0, 0, 0, 1), 0, 0, 1, listOf(0)), replacement)
		val program6 = replaceExpression(program5, createExpressionAddress(listOf(0, 1, 0, 0), 0, 0, 1, listOf(0)), replacement)
		val program7 = replaceExpression(program6, createExpressionAddress(listOf(0, 1, 0, 1), 0, 0, 1, listOf(0)), replacement)

		fun createReplacedBranch1(id: Int) = createCompound(id, basicStatement = Assignment(VariableAccess(retVariable), createPrintStringMethod(replacement)))
		fun createReplacedBranch0(firstId: Int) = createCompound(
			statement = IfStatement(
				createExpressionBlock(firstId, expression = BooleanLiteralFalse),
				createReplacedBranch1(firstId + 1),
				createReplacedBranch1(firstId + 2)
			)
		)

		Assertions.assertThat(program7).isEqualTo(
			createProgram(
				listOf(
					IfStatement(
						createExpressionBlock(1, statement = compoundStatement0basicStatement0, expression = BooleanLiteralFalse),
						createReplacedBranch0(2),
						createReplacedBranch0(5)
					),
					ReturnStatement(createExpressionBlock(8, expression = IntegerLiteral.p0))
				),
			)
		)
	}
}