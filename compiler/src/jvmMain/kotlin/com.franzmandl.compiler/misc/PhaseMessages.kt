package com.franzmandl.compiler.misc

import com.franzmandl.compiler.common.JsonFormat
import com.franzmandl.compiler.common.Phase
import com.franzmandl.compiler.common.PhaseMessage

class PhaseMessages<T : PhaseMessage>(
	private val phase: Phase,
	private val previousPhaseMessages: PhaseMessages<out PhaseMessage>?,
	private val isWarning: Boolean,
) {
	val messages = mutableListOf<T>()

	fun check() {
		previousPhaseMessages?.check()
		if (messages.size != 0) {
			throw PhaseException(this)
		}
	}

	fun add(error: T) {
		val index = messages.indexOfFirst { it.location.line > error.location.line }
		if (index < 0) {
			messages.add(error)
		} else {
			messages.add(index, error)
		}
	}

	fun hasNoErrors() = messages.isEmpty()

	private fun appendString(stringBuilder: StringBuilder, lineSeparator: String = "\n") {
		messages.sortBy { it.location.line }
		if (previousPhaseMessages != null) {
			previousPhaseMessages.appendString(stringBuilder)
			if (previousPhaseMessages.messages.isNotEmpty()) {
				return
			}
		}
		val count = messages.size
		stringBuilder.append(lineSeparator)
		stringBuilder.append("Number of ").append(phase.prefix).append(" ").append(if (isWarning) "warnings" else "errors").append(": ").append(count)
		stringBuilder.append(lineSeparator)
		for ((counter, message) in messages.withIndex()) {
			stringBuilder.append("  #").append(counter + 1).append(": ")
			if (phase === Phase.TypeChecker && !isWarning) {
				stringBuilder.append(message.text).append(" (line ").append(message.location.line).append(")")
			} else {
				stringBuilder.append("line ").append(message.location).append(" ").append(message.text)
			}
			stringBuilder.append(lineSeparator)
		}
	}

	fun toJsonString() =
		"{\"${JsonFormat.classDiscriminator}\":\"PhaseMessages\"" +
				",\"phase\":" + JsonFormat.encodeToString(phase) +
				",\"messages\":" + JsonFormat.encodeToString<List<PhaseMessage>>(messages) +
				",\"isWarning\":${isWarning}}"

	override fun toString(): String {
		val stringBuilder = StringBuilder()
		appendString(stringBuilder)
		return stringBuilder.toString()
	}
}