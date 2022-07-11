package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.ast.Type
import com.franzmandl.compiler.common.ParserError
import com.franzmandl.compiler.common.Phase
import com.franzmandl.compiler.common.TypeError
import com.franzmandl.compiler.common.TypeWarning
import com.franzmandl.compiler.generated.JovaParser.ProgramContext
import com.franzmandl.compiler.misc.PhaseMessages
import com.franzmandl.compiler.misc.SerializationPhase

class TypeCheckerPhase(
	private val fileName: String,
	private val programCtx: ProgramContext,
	val parserErrors: PhaseMessages<ParserError>,
) {
	fun checkTypes(): SerializationPhase {
		parserErrors.check()
		val typeErrors = PhaseMessages<TypeError>(Phase.TypeChecker, parserErrors, false)
		val typeWarnings = PhaseMessages<TypeWarning>(Phase.TypeChecker, typeErrors, true)
		val program = ProgramSymbolTable(fileName)
		var needsScanner = false
		// Visit in three passes: context --map--> first pass --flatMap--> second pass --forEach--> third pass
		programCtx.clazz().map { classCtx ->
			val classId = classCtx.classHead().CLASS_TYPE()
			val type = Type(classId.text)
			if (!program.addType(type)) {
				typeErrors.add(TypeError.ClassDoubleDefinitionTypeError(Util.createLocation(classId), type.id))
			}
			{
				val clazz = ClassSymbolTable(type)
				program.addClass(clazz)
				ClassVisitor(clazz, typeErrors, { needsScanner = true }, program, typeWarnings).visit(classCtx.classBody())
			}
		}.flatMap { it() }.forEach { it() }
		return SerializationPhase(program.toAst(needsScanner), typeErrors, typeWarnings)
	}
}