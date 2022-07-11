package com.franzmandl.compiler.suite

import com.franzmandl.compiler.optimizer.Optimization

object Constant {
	const val settingsFileName = "settings.json"
	const val errTxtFileName = "err.txt"
	const val exitTxtFileName = "exit.txt"
	const val inJovaFileName = "in.jova"
	const val inJsonFileName = "in.json"
	const val inTxtFileName = "in.txt"
	const val msgTxtFileName = "msg.txt"
	const val outDirectoryName = "out"
	const val outJovaFileName = "out.jova"
	const val outTxtFileName = "out.txt"
	const val refDirectoryName = "ref"
	const val testCasesDirectory = "src/jvmTest/resources/cases"
	val testCasesDirectories = listOf(testCasesDirectory) + (System.getenv("TEST_CASES_PATH") ?: "").split(",").filter { it.isNotEmpty() }
	const val updateWebJson = false  // default: false
	const val updateRef = false  // default: false
	val optimizationsList = listOf(Optimization.values().toSet() - setOf(Optimization.ThreeAddressCode), Optimization.values().toSet())  // default

	//val optimizationsList = Optimization.values().map { setOf(it) }
	//val optimizationsList = listOf<Set<Optimization>>()
	const val compareAst = true  // default: true
	const val compareFormatted = true  // default: true
}