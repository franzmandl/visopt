package com.franzmandl.compiler.suite

import com.franzmandl.compiler.Compiler
import com.franzmandl.compiler.common.JsonFormat
import com.franzmandl.compiler.common.LoopMode
import com.franzmandl.compiler.ctx.Command
import com.franzmandl.compiler.ctx.ProgramAddress
import com.franzmandl.compiler.misc.JasminFileSavePhase.generateClassFiles
import com.franzmandl.compiler.misc.JavaRunPhase
import com.franzmandl.compiler.suite.Util.assertCommandIntegrity
import org.assertj.core.api.Assertions
import java.io.File
import java.util.stream.Stream
import kotlin.streams.asStream

class TestedPhase(
	val phaseDirectoryName: String,
	val defaultMsgTxt: String,
	private val testCaseLogic: (TestCase, UseWorkingDirectory) -> Unit,
) {
	fun getTestCaseStream(): Stream<TestCase> =
		Constant.testCasesDirectories.fold(Stream.of()) { stream, testCaseDirectory ->
			val phaseDirectory = File(testCaseDirectory, phaseDirectoryName)
			val settingsFile = File(phaseDirectory, Constant.settingsFileName)
			val settings = if (settingsFile.exists()) {
				JsonFormat.decodeFromString(settingsFile.readText())
			} else {
				TestCaseDirectory(false, mapOf(), "")
			}
			Stream.concat(stream, File(phaseDirectory, settings.subDirectoryName).walk()
				.filter { it.name == Constant.inJovaFileName }
				.map { TestCase(phaseDirectory, defaultMsgTxt, it) }
				.filter { testCase -> settings.invertExcluded == settings.excludedPrefixes.keys.any { testCase.name.startsWith(it) } }
				.asStream())
		}

	fun runTestCase(testCase: TestCase, useWorkingDirectory: UseWorkingDirectory) {
		// IntelliJ makes paths clickable, therefore we print it, so you can easily go to the corresponding test case files.
		println(testCase.name + "\n" + testCase.inJovaFile.path)
		testCaseLogic(testCase, useWorkingDirectory)
	}

	companion object {
		val lexer = TestedPhase(
			"lexer", "\n" +
					"Number of lexical errors: 0\n"
		) { testCase, _ ->
			val result = Compiler.fromFileName(testCase.inJovaFile.path).getAllTokens()
			testCase.refMsgTxt.assertRef(result.toString())
		}

		val parser = TestedPhase(
			"parser", lexer.defaultMsgTxt + "\n" +
					"Number of syntax errors: 0\n"
		) { testCase, _ ->
			val result = Compiler.fromFileName(testCase.inJovaFile.path).parser()
			testCase.refMsgTxt.assertRef(result.parserErrors.toString())
		}

		val typeChecker = TestedPhase(
			"type_checker", parser.defaultMsgTxt + "\n" +
					"Number of type errors: 0\n" +
					"\n" +
					"Number of type warnings: 0\n"
		) { testCase, useWorkingDirectory ->
			val result = Compiler.fromFileName(testCase.inJovaFile.path).checkTypes()
			testCase.refMsgTxt.assertRef(result.typeWarnings.toString())
			if (result.typeErrors.hasNoErrors()) {
				val formatted = result.formatJova(ProgramAddress)
				if (testCase.refOutJova.exists()) {
					Assertions.assertThat(formatted).isEqualTo(testCase.refOutJova.readText())
				}
				if (useWorkingDirectory == UseWorkingDirectory.UseOutDirectory) {
					useWorkingDirectory.useDirectory(testCase) { workingDirectory ->
						File(workingDirectory, Constant.outJovaFileName).writeText(formatted)
					}
				}
				Assertions.assertThat(result.program).isEqualTo(Compiler.fromString(result.program.fileName, formatted).checkTypes().program)
			}
		}

		val code = TestedPhase("code", typeChecker.defaultMsgTxt) { testCase, useWorkingDirectory ->
			val serializationPhase = Compiler.fromFileName(testCase.inJovaFile.path).checkTypes()
			testCase.refMsgTxt.assertRef(serializationPhase.typeWarnings.toString())
			useWorkingDirectory.useDirectory(testCase) { workingDirectory ->
				val formatted = serializationPhase.formatJova(ProgramAddress)
				if (testCase.refOutJova.exists()) {
					Assertions.assertThat(formatted).isEqualTo(testCase.refOutJova.readText())
				}
				if (useWorkingDirectory == UseWorkingDirectory.UseOutDirectory) {
					File(workingDirectory, Constant.outJovaFileName).writeText(formatted)
				}
				Assertions.assertThat(serializationPhase.program).isEqualTo(Compiler.fromString(serializationPhase.program.fileName, formatted).checkTypes().program)
				serializationPhase.generateClassFiles(workingDirectory.path)?.let { runMain(testCase, it) }
				val jsonString = serializationPhase.toJsonString()
				if (Constant.updateWebJson) {
					testCase.inJsonFile.writeText(jsonString)
				}
				for (optimizations in Constant.optimizationsList) {
					val commands = mutableListOf<Command>()
					val optimizerPhase =
						Compiler.fromJsonString(jsonString).optimize(commands::add, LoopMode.infinite, optimizations, ProgramAddress, setOf())
					optimizerPhase.generateJasmin().optimizeRedundantStackInstructionPeephole().generateClassFiles(workingDirectory.path)?.let { runMain(testCase, it) }
					assertCommandIntegrity(serializationPhase.program, optimizerPhase.program, commands)
				}
			}
		}

		private fun runMain(testCase: TestCase, runPhase: JavaRunPhase) {
			val result = runPhase.runCaptured(testCase.inTxtFile)
			testCase.refErrTxt.assertRef(result.stderr)
			testCase.refOutTxt.assertRef(result.stdout)
			testCase.refExit.assertRef(result.exitValue.toString())
		}
	}
}