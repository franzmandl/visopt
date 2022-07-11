package com.franzmandl.compiler.server

import com.franzmandl.compiler.Compiler
import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ast.Clazz
import com.franzmandl.compiler.code.jasmin.JasminInstruction
import com.franzmandl.compiler.code.jasmin.JasminSignature
import com.franzmandl.compiler.code.jasmin.JasminType
import com.franzmandl.compiler.common.*
import com.franzmandl.compiler.ctx.*
import com.franzmandl.compiler.optimizer.Optimization
import com.franzmandl.compiler.reflection.*
import com.franzmandl.compiler.reflection.Member
import com.franzmandl.compiler.reflection.Util
import com.franzmandl.compiler.suite.Util.all01fileName
import org.junit.jupiter.api.Test
import java.io.File

class GenerateWebSourcesTests {
	private val webPath = "../web"
	private val webExamplesPath = "$webPath/src/examples"
	private val webGeneratedIndexDTsPath = "$webPath/@types/compiler-generated/index.d.ts"

	@Test
	fun exampleAll01() {
		val phase = Compiler.fromFileName(all01fileName).checkTypes().optimize()
		phase.apply { program = Program("example.jova", program.needsScanner, program.classes.filter { it.id != Clazz.mainId }) }
		val jovaString = phase.formatJova(ProgramAddress).replace("\\n", "\\\\n")
		val jsonString = phase.toJsonString()
		File("$webExamplesPath/all01.ts").writeText(
			"import {TypeCheckerResult} from 'model/TypeCheckerResult';\n\n" +
					"// prettier-ignore\n" +
					"export const jovaString = `$jovaString`;\n\n" +
					"// prettier-ignore\n" +
					"export const typeCheckerResult: TypeCheckerResult = $jsonString;\n"
		)
	}

	@Test
	fun indexDTs() {
		val declaredTypes = mutableSetOf("boolean", "number", "string")

		fun declareType(type: String) {
			if (!declaredTypes.add(type)) {
				Util.throwIllegalStateException("Type '$type' already declared")
			}
		}

		val usedTypes = mutableSetOf<String>()
		val classes = listOf(
			DataClass(::declareType, usedTypes::add, Body::class),
			DataClass(::declareType, usedTypes::add, BodyInfo::class),
			DataClass(::declareType, usedTypes::add, BodyInfoChange::class),
			DataClass(::declareType, usedTypes::add, Cfg::class),
			DataClass(::declareType, usedTypes::add, CfgNode::class),
			DataClass(::declareType, usedTypes::add, Clazz::class),
			DataClass(::declareType, usedTypes::add, Compound::class),
			DataClass(::declareType, usedTypes::add, ConstructorSignature::class),
			DataClass(::declareType, usedTypes::add, ExpressionBlock::class),
			DataClass(::declareType, usedTypes::add, JasminSignature::class),
			DataClass(::declareType, usedTypes::add, Location::class),
			DataClass(::declareType, usedTypes::add, LoopMode::class),
			DataClass(::declareType, usedTypes::add, MappingEntry::class),
			DataClass(::declareType, usedTypes::add, MethodSignature::class),
			DataClass(::declareType, usedTypes::add, Program::class),
			DataClass(::declareType, usedTypes::add, Signature::class),
			DataClass(::declareType, usedTypes::add, SignatureNullable::class),
			DataClass(::declareType, usedTypes::add, Variable::class),
			EnumClass(::declareType, AccessModifier::class) { it.modifier },
			EnumClass(::declareType, ArithmeticBinaryOperator::class) { it.sign },
			EnumClass(::declareType, ArithmeticUnaryOperator::class) { it.sign },
			EnumClass(::declareType, LogicalBinaryOperator::class) { it.sign },
			EnumClass(::declareType, ObjectEqualsBinaryOperator::class) { it.sign },
			EnumClass(::declareType, Optimization::class) { it.name },
			EnumClass(::declareType, Phase::class) { it.name },
			EnumClass(::declareType, RelationalBinaryOperator::class) { it.sign },
			SealedClass(::declareType, usedTypes::add, Address::class),
			SealedClass(::declareType, usedTypes::add, BasicStatement::class),
			SealedClass(::declareType, usedTypes::add, BuiltinMethod::class),
			SealedClass(::declareType, usedTypes::add, ClassSymbol::class),
			SealedClass(::declareType, usedTypes::add, Command::class),
			SealedClass(::declareType, usedTypes::add, CompoundStatement::class),
			SealedClass(::declareType, usedTypes::add, Expression::class),
			SealedClass(::declareType, usedTypes::add, JasminInstruction::class),
			SealedClass(::declareType, usedTypes::add, JasminType::class),
			SealedClass(::declareType, usedTypes::add, PhaseMessage::class),
			SealedClass(::declareType, usedTypes::add, ReplaceExpressionReason::class),
			SpecialClass(
				::declareType, BinaryOperands::class, listOf(
					Member("lhs", Util.getTypeScriptType(Expression::class, usedTypes::add), isOptional = false, isNullable = false),
					Member("rhs", Util.getTypeScriptType(Expression::class, usedTypes::add), isOptional = false, isNullable = false),
				)
			),
		)
		val unknownTypes = usedTypes - declaredTypes
		if (unknownTypes.isNotEmpty()) {
			Util.throwIllegalStateException("There are unknown types: ${unknownTypes.joinToString(", ")}")
		}
		File(webGeneratedIndexDTsPath).bufferedWriter().use {
			it.write("// prettier-ignore\ndeclare module 'compiler-generated' {")
			for (clazz in classes) {
				clazz.append(it::write)
			}
			it.write("\n}\n")
		}
	}
}