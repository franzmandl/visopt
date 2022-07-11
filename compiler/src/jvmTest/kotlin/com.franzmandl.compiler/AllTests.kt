package com.franzmandl.compiler

import com.franzmandl.compiler.suite.TestCase
import com.franzmandl.compiler.suite.TestedPhase
import com.franzmandl.compiler.suite.UseWorkingDirectory
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
class AllTests {
	@Order(1)
	@ParameterizedTest(name = "{0}")
	@MethodSource
	fun lexer(testCase: TestCase) = TestedPhase.lexer.runTestCase(testCase, UseWorkingDirectory.UseTempDirectory)
	fun lexer() = TestedPhase.lexer.getTestCaseStream()

	@Order(2)
	@ParameterizedTest(name = "{0}")
	@MethodSource
	fun parser(testCase: TestCase) = TestedPhase.parser.runTestCase(testCase, UseWorkingDirectory.UseTempDirectory)
	fun parser() = TestedPhase.parser.getTestCaseStream()

	@Order(3)
	@ParameterizedTest(name = "{0}")
	@MethodSource
	fun typeChecker(testCase: TestCase) = TestedPhase.typeChecker.runTestCase(testCase, UseWorkingDirectory.UseTempDirectory)
	fun typeChecker() = TestedPhase.typeChecker.getTestCaseStream()

	@Order(4)
	@ParameterizedTest(name = "{0}")
	@MethodSource
	fun code(testCase: TestCase) = TestedPhase.code.runTestCase(testCase, UseWorkingDirectory.UseTempDirectory)
	fun code() = TestedPhase.code.getTestCaseStream()
}