package com.franzmandl.compiler.misc

import com.franzmandl.compiler.common.PhaseMessage

class PhaseException(val messages: PhaseMessages<out PhaseMessage>) : Exception() {
	override fun toString() = messages.toString()
}