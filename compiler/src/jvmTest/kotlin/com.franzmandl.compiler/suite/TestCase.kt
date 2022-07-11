package com.franzmandl.compiler.suite

import org.assertj.core.api.Assertions
import java.io.File

class TestCase(
	phaseDirectory: File,
	defaultMsgTxt: String,
	val inJovaFile: File,
) {
	private val testCaseDirectory: File = inJovaFile.parentFile
	val name = testCaseDirectory.path.drop(phaseDirectory.path.length + 1) + "/"
	val inJsonFile = File(testCaseDirectory, Constant.inJsonFileName)
	val inTxtFile = File(testCaseDirectory, Constant.inTxtFileName).takeIf { it.exists() }
	val outDirectory = File(testCaseDirectory, Constant.outDirectoryName)
	private val refDirectory = File(testCaseDirectory, Constant.refDirectoryName)
	val refErrTxt = RefFile(refDirectory, Constant.errTxtFileName, "")
	val refExit = RefFile(refDirectory, Constant.exitTxtFileName, "0")
	val refMsgTxt = RefFile(refDirectory, Constant.msgTxtFileName, defaultMsgTxt)
	val refOutJova = File(refDirectory, Constant.outJovaFileName)
	val refOutTxt = RefFile(refDirectory, Constant.outTxtFileName, "")

	override fun toString() = name

	class RefFile(
		private val file: File,
		private val defaultText: String,
	) {
		constructor(refDirectory: File, filename: String, defaultText: String) : this(File(refDirectory, filename), defaultText)

		fun assertRef(actual: String) {
			if (Constant.updateRef) {
				if (actual == defaultText) {
					if (file.exists()) {
						file.delete()
					}
				} else {
					file.writeText(actual)
				}
			} else {
				Assertions.assertThat(actual).isEqualTo(if (file.exists()) file.readText() else defaultText)
			}
		}
	}
}