package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.ast.Signature
import com.franzmandl.compiler.ast.Type
import com.franzmandl.compiler.ast.Variable
import com.franzmandl.compiler.common.Location
import com.franzmandl.compiler.common.SymbolTable
import com.franzmandl.compiler.common.TypeError
import com.franzmandl.compiler.common.TypeError.VariableDoubleDefinitionTypeError
import com.franzmandl.compiler.misc.PhaseMessages

class ScopeSymbolTable(
	private val parent: ScopeSymbolTable?,
) {
	private val variables: SymbolTable<Variable> = SymbolTable()

	fun createAndAddVariable(id: String, location: Location, level: Int, type: Type, signature: Signature, errors: PhaseMessages<TypeError>): Variable {
		val variable = Variable(id, level, type)
		if (!variables.add(variable)) {
			errors.add(VariableDoubleDefinitionTypeError(location, id, type.id, signature))
		}
		return variable
	}

	fun getVariable(id: String): Variable? = variables[id] ?: parent?.getVariable(id)
}