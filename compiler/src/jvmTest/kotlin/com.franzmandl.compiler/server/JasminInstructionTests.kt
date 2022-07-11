package com.franzmandl.compiler.server

import com.franzmandl.compiler.Compiler
import com.franzmandl.compiler.code.jasmin.InstructionFormatter
import com.franzmandl.compiler.suite.Constant
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File

class JasminInstructionTests {
	@Test
	fun all01() {
		val testCaseDirectory = "${Constant.testCasesDirectory}/code/generator/all01"
		val classes = Compiler.fromFileName("${testCaseDirectory}/in.jova").checkTypes().generateJasmin().classes
		val builder = StringBuilder()
		val formatter = InstructionFormatter(builder::append).apply { appendAddress = true }
		for (clazz in classes) {
			clazz.appendInstructions(formatter::appendInstruction)
		}
		val refFile = File("${testCaseDirectory}/ref/jasmin.j")
		if (Constant.updateRef) {
			refFile.writeText(builder.toString())
		} else {
			// For debugging: File("${testCaseDirectory}/out/jasmin.j").writeText(instructionsString)
			Assertions.assertThat(builder.toString()).isEqualTo(refFile.readText())
		}
	}
}