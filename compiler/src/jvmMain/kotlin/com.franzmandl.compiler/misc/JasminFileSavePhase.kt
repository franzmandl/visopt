package com.franzmandl.compiler.misc

import com.franzmandl.compiler.code.jasmin.JasminOptimizerPhase
import java.io.File
import java.io.IOException

object JasminFileSavePhase {
	fun JasminOptimizerPhase.generateClassFiles(directoryName: String) = saveFiles(directoryName).generateClassFiles()

	fun SerializationPhase.generateClassFiles(directoryName: String) = saveFiles(directoryName).generateClassFiles()

	fun JasminOptimizerPhase.saveFiles(directoryName: String): JasminGeneratorPhase {
		val directory = File(directoryName)
		if (!directory.exists() && !directory.mkdirs()) {
			throw IOException("Could not create directory.")
		}
		for (clazz in classes) {
			File(directory, clazz.fileName).bufferedWriter().use {
				clazz.appendStrings(it::write)
			}
		}
		return JasminGeneratorPhase(directory)
	}

	fun SerializationPhase.saveFiles(directoryName: String) = generateJasmin().saveFiles(directoryName)
}