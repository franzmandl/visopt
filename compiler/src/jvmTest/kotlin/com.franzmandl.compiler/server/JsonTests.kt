package com.franzmandl.compiler.server

import com.franzmandl.compiler.Compiler
import com.franzmandl.compiler.suite.Constant
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode

@Execution(ExecutionMode.CONCURRENT)
class JsonTests {
	@Test
	fun testWarnings() {
		val jsonString = Compiler.fromFileName("${Constant.testCasesDirectory}/type_checker/public/coercion_warning/warning01/in.jova").checkTypes().toJsonString()
		val compilerPhase = Compiler.fromJsonString(jsonString)
		Assertions.assertThat(compilerPhase).isNotNull
	}
}