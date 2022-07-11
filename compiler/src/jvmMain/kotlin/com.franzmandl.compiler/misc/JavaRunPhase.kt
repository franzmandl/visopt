package com.franzmandl.compiler.misc

import com.franzmandl.compiler.ast.Clazz
import java.io.File
import java.util.concurrent.TimeUnit

class JavaRunPhase(
	private val directory: File,
) {
	fun runCaptured(stdin: File?): ProcessResult {
		val processBuilder = ProcessBuilder("java", "-cp", directory.path, Clazz.mainId)
		if (stdin != null) {
			processBuilder.redirectInput(stdin)
		}
		return ProcessResult(processBuilder.start(), "${directory.path}: Run process timeout.")
	}

	fun runRedirected(stdin: File?, stdout: File?, stderr: File?, exitFile: File?) {
		val processBuilder = ProcessBuilder("java", "-cp", directory.path, Clazz.mainId)
		if (stdout != null) {
			processBuilder.redirectOutput(stdout)
		}
		if (stderr != null) {
			processBuilder.redirectError(stderr)
		}
		if (stdin != null) {
			processBuilder.redirectInput(stdin)
		}
		val process = processBuilder.start()
		if (!process.waitFor(5, TimeUnit.SECONDS)) {
			process.destroy()
			throw IllegalStateException("${directory.path}: Run process timeout.")
		}
		val exitValue = process.exitValue()
		exitFile?.writeText(exitValue.toString())
	}
}