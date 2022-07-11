package com.franzmandl.compiler.suite

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.code.jova.JovaFormatter
import com.franzmandl.compiler.common.*
import com.franzmandl.compiler.ctx.*
import com.franzmandl.compiler.generated.JovaLexer
import com.franzmandl.compiler.generated.JovaParser
import com.franzmandl.compiler.misc.PhaseMessages
import com.franzmandl.compiler.optimizer.Optimization
import com.franzmandl.compiler.type_checker.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.assertj.core.api.Assertions

object Util {
	const val dummyFileName = "noSource"
	const val dummyClassId = "Main"
	private val dummyType = Type(dummyClassId)
	const val dummyMethodName = "main"
	private val dummySignature = Signature(dummyMethodName, listOf())
	val dummyBodyAddress = BodyAddress(dummyClassId, dummySignature)
	val dummyCompoundAddress = CompoundAddress(dummyBodyAddress, listOf())
	val dummyBasicStatement = ExpressionStatement(NixLiteral)
	val dummyOptimization = Optimization.ConstantPropagation

	const val all01fileName = "${Constant.testCasesDirectory}/code/generator/all01/in.jova"
	private const val all01classId = "Example"
	val all01allOptimizationsAddress = BodyAddress(all01classId, Signature("allOptimizations", listOf()))
	val all01algebraicSimplificationsAddress = BodyAddress(all01classId, Signature("algebraicSimplifications", listOf()))
	val all01commonSubexpressionEliminationAddress = BodyAddress(all01classId, Signature("commonSubexpressionElimination", listOf()))
	val all01constantFoldingAddress = BodyAddress(all01classId, Signature("constantFolding", listOf()))
	val all01constantPropagationAddress = BodyAddress(all01classId, Signature("constantPropagation", listOf()))
	val all01copyPropagationAddress = BodyAddress(all01classId, Signature("copyPropagation", listOf()))
	val all01deadCodeEliminationAddress = BodyAddress(all01classId, Signature("deadCodeElimination", listOf()))
	val all01reductionInStrengthAddress = BodyAddress(all01classId, Signature("reductionInStrength", listOf()))
	val all01unreachableCodeEliminationAddress = BodyAddress(all01classId, Signature("unreachableCodeElimination", listOf()))
	val all01addresses = mapOf(
		all01allOptimizationsAddress to Optimization.values().filter { it !== Optimization.ThreeAddressCode }.toSet(),
		all01algebraicSimplificationsAddress to setOf(Optimization.AlgebraicSimplifications),
		all01commonSubexpressionEliminationAddress to setOf(Optimization.CommonSubexpressionElimination),
		all01constantFoldingAddress to setOf(Optimization.ConstantFolding),
		all01constantPropagationAddress to setOf(Optimization.ConstantPropagation),
		all01copyPropagationAddress to setOf(Optimization.CopyPropagation),
		all01deadCodeEliminationAddress to setOf(Optimization.DeadCodeElimination),
		all01reductionInStrengthAddress to setOf(Optimization.ReductionInStrength),
		all01unreachableCodeEliminationAddress to setOf(Optimization.DeadCodeElimination),
	)

	fun createExpressionAddress(compoundIndices: List<Int>, compoundStatementIndex: Int, basicStatementIndex: Int, rootIndex: Int, expressionIndices: List<Int>) =
		ExpressionAddress(
			BasicStatementAddress(BasicBlockAddress(CompoundStatementAddress(CompoundAddress(dummyBodyAddress, compoundIndices), compoundStatementIndex)), basicStatementIndex),
			rootIndex,
			expressionIndices
		)

	fun createProgram(body: Body) =
		Program(dummyFileName, false, listOf(Clazz(dummyClassId, listOf(Method(MethodSignature(AccessModifier.Public, true, dummySignature, Type.int), body)))))

	fun createProgram(statements: List<CompoundStatement> = listOf()) =
		createProgram(createBody(statements = statements))

	fun createProgram(returnExpressionBlock: ExpressionBlock) =
		createProgram(listOf(ReturnStatement(returnExpressionBlock)))

	fun createBody(arguments: List<Variable> = listOf(), statements: List<CompoundStatement> = listOf()): Body {
		val compound = Compound(statements)
		return Body(arguments, compound, CfgBuilder.build(compound), BodyInfo())
	}

	fun createBasicBlock(id: Int, statements: List<BasicStatement> = listOf()) = BasicBlock(id, statements)

	fun createBasicBlock(id: Int, statement: BasicStatement) = createBasicBlock(id, listOf(statement))

	fun createExpressionBlock(id: Int, statements: List<BasicStatement> = listOf(), expression: Expression) =
		ExpressionBlock(createBasicBlock(id, statements), expression)

	fun createExpressionBlock(id: Int, statement: BasicStatement, expression: Expression) =
		createExpressionBlock(id, listOf(statement), expression)

	fun createCompound(statement: CompoundStatement) = Compound(listOf(statement))

	fun createCompound(id: Int, basicStatement: BasicStatement) =
		createCompound(createBasicBlock(id, basicStatement))

	fun createPrintStringMethod(argument: Expression) = BuiltinMethodInvocation(PrintIntMethod, listOf(argument))

	fun replaceExpression(program: Program, address: ExpressionAddress, replacement: Expression) =
		ReplaceExpression(dummyOptimization, null, address, BooleanLiteralFalse, replacement, RuleReplaceExpressionReason("test", "test"), null).apply(program)

	fun parseBody(input: String, arguments: List<Variable> = listOf()): Body {
		val lexerErrors = PhaseMessages<LexerError>(Phase.Lexer, null, false)
		val lexer = JovaLexer(CharStreams.fromString("{$input}"))
		ErrorListener.setLexerErrorListener(lexer, lexerErrors::add)
		val parserErrors = PhaseMessages<ParserError>(Phase.Parser, lexerErrors, false)
		val parser = JovaParser(CommonTokenStream(lexer))
		ErrorListener.setParserErrorListener(parser, parserErrors::add)
		val ctx = parser.constructorBody()
		parserErrors.check()
		val clazz = ClassSymbolTable(dummyType)
		val typeErrors = PhaseMessages<TypeError>(Phase.TypeChecker, parserErrors, false)
		val typeWarnings = PhaseMessages<TypeWarning>(Phase.TypeChecker, typeErrors, true)
		val program = ProgramSymbolTable(dummyFileName)
		val scope = ScopeSymbolTable(null)
		val bodyInfo = BodyInfo()
		val compound = ScopeVisitor(clazz, typeErrors, bodyInfo, 0, {}, program, scope, dummySignature, typeWarnings)
			.visit(ctx.variable(), ctx.statement(), null)
		typeErrors.check()
		return Body(arguments, compound, CfgBuilder.build(compound), bodyInfo)
	}

	fun assertCommandIntegrity(firstProgram: Program, expectedLastProgram: Program, commands: List<Command>) {
		assertProgramIntegrity(firstProgram)
		val programs = mutableListOf(firstProgram)
		for (command in commands) {
			val currentProgram = programs.last()
			val appliedProgram = command.apply(currentProgram)
			assertProgramIntegrity(appliedProgram)
			programs.add(appliedProgram)
			val revertedProgram = command.revert(appliedProgram)
			if (revertedProgram != null) {
				assertProgramIntegrity(revertedProgram)
				Assertions.assertThat(revertedProgram).isEqualTo(currentProgram)
			}
		}
		if (Constant.compareFormatted) {
			Assertions.assertThat(JovaFormatter.format(programs.last(), ProgramAddress)).isEqualTo(JovaFormatter.format(expectedLastProgram, ProgramAddress))
		}
		if (Constant.compareAst) {
			Assertions.assertThat(programs.last()).isEqualTo(expectedLastProgram)
		}
	}

	private fun assertProgramIntegrity(program: Program) {
		for (clazz in program.classes) {
			for (symbol in clazz.symbols) {
				if (symbol is HasBodySymbol) {
					val compoundStatements = symbol.body.compound.statements
					for (index in compoundStatements.indices) {
						val previousCompoundStatement = compoundStatements.getOrNull(index - 1)
						when (val currentCompoundStatement = compoundStatements[index]) {
							is BasicBlock -> {
								Assertions.assertThat(currentCompoundStatement.statements).isNotEmpty
								Assertions.assertThat(previousCompoundStatement !is BasicBlock).isTrue
							}
							is IfStatement, is ReturnStatement -> Assertions.assertThat(previousCompoundStatement !is BasicBlock).isTrue
							is WhileStatement -> {}
						}
					}
				}
			}
		}
	}
}