package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.ast.Program
import com.franzmandl.compiler.ast.Type
import com.franzmandl.compiler.common.SymbolTable
import com.franzmandl.compiler.common.TypeError
import com.franzmandl.compiler.common.TypeError.MainInstantiationError
import com.franzmandl.compiler.common.TypeError.UnknownTypeError
import com.franzmandl.compiler.generated.JovaParser.ParametersContext
import com.franzmandl.compiler.generated.JovaParser.TypeContext
import com.franzmandl.compiler.misc.PhaseMessages

class ProgramSymbolTable(private val fileName: String) {
	private val classes = SymbolTable<ClassSymbolTable>()
	private val types = mutableSetOf(Type.bool, Type.int, Type.string)

	fun addClass(clazz: ClassSymbolTable) = classes.add(clazz)

	fun addType(type: Type) = types.add(type)

	fun getClass(type: Type?) = if (type != null) classes[type.id] else null

	fun getType(ctx: TypeContext, errors: PhaseMessages<TypeError>): Type? {
		val type = Type(ctx.text)
		if (type.isMain) {
			errors.add(MainInstantiationError(Util.createLocation(ctx)))
			return null
		}
		if (type !in types) {
			errors.add(UnknownTypeError(Util.createLocation(ctx), type.id))
			return null
		}
		return type
	}

	fun getTypes(ctx: ParametersContext, errors: PhaseMessages<TypeError>): List<Type>? {
		val typesCtx = ctx.parameterList()?.types
		val types = typesCtx?.mapNotNull { getType(it, errors) } ?: listOf()
		return if ((typesCtx?.size ?: 0) == types.size) types else null
	}

	fun hasClass(type: Type?) = type != null && type.id in classes

	fun toAst(needsScanner: Boolean) = Program(fileName, needsScanner, classes.map { it.toAst() })
}