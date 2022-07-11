package com.franzmandl.compiler.server

import com.franzmandl.compiler.suite.Constant
import com.franzmandl.compiler.suite.TestCase
import com.franzmandl.compiler.suite.TestedPhase
import com.franzmandl.compiler.suite.UseWorkingDirectory
import org.junit.jupiter.api.Test
import java.io.File

class SingleTests {
	private fun runTestCase(phase: TestedPhase, jovaFileName: String) {
		val phaseDirectory = File(Constant.testCasesDirectory, phase.phaseDirectoryName)
		phase.runTestCase(TestCase(phaseDirectory, phase.defaultMsgTxt, File(phaseDirectory, jovaFileName)), UseWorkingDirectory.UseTempDirectory)
	}

	@Test
	fun all01() =
		runTestCase(TestedPhase.code, "generator/all01/in.jova")

	@Test
	fun complex01() =
		runTestCase(TestedPhase.code, "generator/complex01/in.jova")
}