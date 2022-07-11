package com.franzmandl.compiler.server

import com.franzmandl.compiler.Compiler
import com.franzmandl.compiler.misc.JasminFileSavePhase.generateClassFiles
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.io.File
import kotlin.system.exitProcess

@SpringBootApplication
open class Server

fun main(args: Array<String>) {
	if (args.isEmpty()) {
		runApplication<Server>(*args)
	} else {
		val actionArgs = args.copyOfRange(1, args.size)
		when (val action = args[0]) {
			"check_types" -> {
				if (actionArgs.size < 2) {
					System.err.println("Usage: $action IN_FILE OUT_FILE")
					exitProcess(1)
				}
				val inFile = actionArgs[0]
				val outFile = actionArgs[1]
				File(outFile).bufferedWriter().use {
					it.write(Compiler.fromFileName(inFile).checkTypes().toJsonString())
				}
			}
			"generate_jasmin" -> {
				if (actionArgs.size < 2) {
					System.err.println("Usage: $action IN_FILE OUT_DIRECTORY")
					exitProcess(1)
				}
				val inFile = actionArgs[0]
				val outDirectory = actionArgs[1]
				Compiler.fromFileName(inFile).checkTypes().generateJasmin().generateClassFiles(outDirectory)
			}
			"server" -> runApplication<Server>(*actionArgs)
			else -> {
				System.err.println("[ERROR] not an action '$action'")
				exitProcess(1)
			}
		}
	}
}