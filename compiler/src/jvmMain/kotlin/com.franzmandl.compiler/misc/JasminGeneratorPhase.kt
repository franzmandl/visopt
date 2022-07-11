package com.franzmandl.compiler.misc

import java.io.File
import java.io.FilenameFilter

class JasminGeneratorPhase(
	private val directory: File,
) {
	fun generateClassFiles(): JavaRunPhase? {
		val filter = FilenameFilter { _, fileName -> fileName.endsWith(".j") }
		val files = directory.listFiles(filter) ?: throw IllegalStateException("${directory.path}: Does not contain children.")
		for (file in files) {
			val result = ProcessResult(
				ProcessBuilder("java", "-jar", "./tools/jasmin.jar", "-d", directory.path, file.path).start(),
				"${file.path}: Jasmin process timeout."
			)
			if (result.exitValue != 0 || result.stderr.isNotEmpty() || !result.stdout.startsWith("Generated: ")) {
				throw IllegalStateException("${file.path}: Jasmin process error: $result")
			}
		}
		return if (File(directory, "Main.class").exists()) JavaRunPhase(directory) else null
	}
}