package com.franzmandl.compiler.reflection

class Member(val name: String, private val type: String, private val isOptional: Boolean, private val isNullable: Boolean) {
	fun append(appendString: (String) -> Unit) {
		appendString("\n        readonly $name")
		if (isOptional) {
			appendString("?")
		}
		appendString(": $type")
		if (isNullable) {
			appendString(" | null")
		}
		appendString(";")
	}

	companion object {
		fun appendMembers(appendString: (String) -> Unit, name: String, members: List<Member>) {
			if (members.isNotEmpty()) {
				appendString("\n\n    export interface $name {")
				for (member in members) {
					member.append(appendString)
				}
				appendString("\n    }")
			}
		}
	}
}