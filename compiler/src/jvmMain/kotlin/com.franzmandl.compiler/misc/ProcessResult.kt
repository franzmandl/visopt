package com.franzmandl.compiler.misc

import java.util.concurrent.TimeUnit

class ProcessResult(process: Process, timeoutExceptionMessage: String) {
	val exitValue: Int
	val stderr: String
	val stdout: String

	init {
		if (!process.waitFor(5, TimeUnit.SECONDS)) {
			process.destroy()
			throw IllegalStateException(timeoutExceptionMessage)
		}
		exitValue = process.exitValue()
		stderr = process.errorStream.bufferedReader().readText()
		stdout = process.inputStream.bufferedReader().readText()
	}

	override fun toString() = "exitValue=$exitValue, stderr='$stderr', stdout='$stdout'"
}