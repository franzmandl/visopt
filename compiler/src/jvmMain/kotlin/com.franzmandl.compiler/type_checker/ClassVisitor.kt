package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.CfgBuilder
import com.franzmandl.compiler.common.SignatureNullable
import com.franzmandl.compiler.common.TypeError
import com.franzmandl.compiler.common.TypeWarning
import com.franzmandl.compiler.generated.JovaParser.*
import com.franzmandl.compiler.misc.PhaseMessages

class ClassVisitor(
	private val clazz: ClassSymbolTable,
	private val errors: PhaseMessages<TypeError>,
	private val needsScanner: () -> Unit,
	private val program: ProgramSymbolTable,
	private val warnings: PhaseMessages<TypeWarning>,
) {
	fun visit(ctx: ClassBodyContext): List<() -> Unit> {
		val bodyInfo = BodyInfo()
		return ctx.children.map { childCtx ->
			when (childCtx) {
				is MemberContext -> visitMember(childCtx, bodyInfo)
				is ConstructorContext -> visitConstructor(childCtx) { bodyInfo.copy() }
				is MethodContext -> visitMethod(childCtx) { bodyInfo.copy() }
				else -> ({})
			}
		}
	}

	private fun visitMember(ctx: MemberContext, bodyInfo: BodyInfo): () -> Unit {
		if (clazz.type.isMain) {
			errors.add(TypeError.MainMemberError(Util.createLocation(ctx)))
		}
		val accessModifier = AccessModifier.modifiers[ctx.AMOD().text] ?: throw UnsupportedOperationException(ctx.AMOD().text)
		val type = program.getType(ctx.type(), errors) ?: return {}
		for (id in ctx.idList().ids) {
			bodyInfo.updateTemporaryVariableCounter(id.text)
			val member = Member(accessModifier, id.text, type)
			if (!clazz.addMember(member)) {
				errors.add(TypeError.MemberDoubleDefinitionTypeError(Util.createLocation(id), id.text, type.id, clazz.id))
			}
		}
		return {}
	}

	private fun visitConstructor(ctx: ConstructorContext, createBodyInfo: () -> BodyInfo): () -> Unit {
		if (clazz.type.isMain) {
			errors.add(TypeError.MainMemberError(Util.createLocation(ctx)))
		}
		val argumentTypes = program.getTypes(ctx.parameters(), errors) ?: return {}
		val signature = Signature(ctx.CLASS_TYPE().text, argumentTypes)
		val constructorSignature = ConstructorSignature(false, signature)
		if (!clazz.addConstructorSignature(constructorSignature)) {
			errors.add(TypeError.MethodDoubleDefinitionTypeError(Util.createLocation(ctx.CLASS_TYPE()), signature, clazz.id))
		}
		return {
			val scope = ScopeSymbolTable(null)
			val arguments = visitMethodParameters(ctx.parameters().parameterList(), scope, signature)
			val bodyInfo = createBodyInfo()
			val compound = ScopeVisitor(clazz, errors, bodyInfo, 0, needsScanner, program, scope, signature, warnings)
				.visit(ctx.constructorBody().variable(), ctx.constructorBody().statement(), null)
			clazz.addConstructor(Constructor(constructorSignature, Body(arguments, compound, CfgBuilder.build(compound), bodyInfo)))
		}
	}

	private fun visitMethod(ctx: MethodContext, createBodyInfo: () -> BodyInfo): () -> Unit {
		val accessModifier = AccessModifier.modifiers[ctx.methodHead().AMOD().text] ?: throw UnsupportedOperationException(ctx.methodHead().AMOD().text)
		val argumentTypes = program.getTypes(ctx.methodHead().parameters(), errors) ?: return {}
		val returnType = program.getType(ctx.methodHead().type(), errors) ?: return {}
		val signature = Signature(ctx.methodHead().ID().text, argumentTypes)
		val isMain = if (clazz.type.isMain) {
			if (accessModifier == AccessModifier.Public && signature == Signature("main", listOf()) && returnType == Type.int) {
				true
			} else {
				errors.add(TypeError.MainMemberError(Util.createLocation(ctx)))
				false
			}
		} else {
			false
		}
		val methodSignature = MethodSignature(accessModifier, isMain, signature, returnType)
		if (SignatureNullable(signature) in Util.builtinMethods || !clazz.addMethodSignature(methodSignature)) {
			errors.add(TypeError.MethodDoubleDefinitionTypeError(Util.createLocation(ctx.methodHead().ID()), signature, clazz.id))
		}
		return {
			val scope = ScopeSymbolTable(null)
			val arguments = visitMethodParameters(ctx.methodHead().parameters().parameterList(), scope, signature)
			val bodyInfo = createBodyInfo()
			val compound = ScopeVisitor(clazz, errors, bodyInfo, 0, needsScanner, program, scope, signature, warnings)
				.visit(ctx.methodBody().variable(), ctx.methodBody().statement(), ctx.methodBody().returnStatement() to returnType)
			clazz.addMethod(Method(methodSignature, Body(arguments, compound, CfgBuilder.build(compound), bodyInfo)))
		}
	}

	private fun visitMethodParameters(ctx: ParameterListContext?, scope: ScopeSymbolTable, signature: Signature) =
		ctx?.let {
			it.ids.mapIndexed { index, id ->
				val type = signature.argumentTypes[index]
				scope.createAndAddVariable(id.text, Util.createLocation(id), 0, type, signature, errors)
			}
		} ?: listOf()
}