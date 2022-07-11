package com.franzmandl.compiler.suite

import java.io.File
import java.io.IOException
import kotlin.io.path.createTempDirectory

interface UseWorkingDirectory {
	fun useDirectory(testCase: TestCase, callback: (File) -> Unit)

	object UseTempDirectory : UseWorkingDirectory {
		override fun useDirectory(testCase: TestCase, callback: (File) -> Unit) {
			val tempDirectory = createTempDirectory().toFile()
			try {
				callback(tempDirectory)
			} finally {
				tempDirectory.deleteRecursively()
			}
		}
	}

	object UseOutDirectory : UseWorkingDirectory {
		override fun useDirectory(testCase: TestCase, callback: (File) -> Unit) {
			if (!testCase.outDirectory.exists() && !testCase.outDirectory.mkdirs()) {
				throw IOException("Could not create directory.")
			}
			callback(testCase.outDirectory)
		}
	}
}