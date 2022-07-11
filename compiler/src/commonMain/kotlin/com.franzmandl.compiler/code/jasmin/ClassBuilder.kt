package com.franzmandl.compiler.code.jasmin

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.Addressed
import com.franzmandl.compiler.ctx.BodyAddress
import com.franzmandl.compiler.ctx.BodyContext

object ClassBuilder {
	fun build(clazz: Clazz, fileName: String, needsScanner: Boolean): JasminClass {
		val builder = mutableListOf<JasminClassSymbol>()
		val methods = mutableListOf<JasminClassSymbol>()
		if (needsScanner && clazz.id == Clazz.mainId) {
			builder.add(JasminMember(Member(AccessModifier.Public, "scanner", Type("java/util/Scanner")), true))
		}
		for (symbol in clazz.symbols) {
			when (symbol) {
				is Constructor -> (if (symbol.constructorSignature.isDefault) builder else methods).add(buildConstructor(clazz.id, symbol, needsScanner))
				is Member -> builder.add(JasminMember(symbol, false))
				is Method -> methods.add(buildMethod(clazz.id, symbol, needsScanner))
			}
		}
		builder.addAll(methods)
		return JasminClass(fileName.substringAfterLast("/"), clazz.id, builder)
	}

	fun buildHasBodySymbol(classId: String, symbol: HasBodySymbol, needsScanner: Boolean) =
		when (symbol) {
			is Constructor -> buildConstructor(classId, symbol, needsScanner)
			is Method -> buildMethod(classId, symbol, needsScanner)
		}

	private fun buildConstructor(classId: String, constructor: Constructor, needsScanner: Boolean): JasminMethod {
		val instructions = mutableListOf<Addressed<out ChangesStack<out StackChange>>>()
		val ctx = BodyContext(BodyAddress(classId, constructor.constructorSignature.signature), constructor)
		instructions.add(ctx.address(Aload0))
		instructions.add(ctx.address(Invokespecial(Type.langObject.id, listOf())))
		ScopeBuilder(instructions::add, false, JasminVoid).appendBody(ctx, needsScanner)
		instructions.add(ctx.address(Return))
		val initSignature = JasminSignature.createInitSignature(JasminSignature.mapArgumentTypes(constructor.constructorSignature.signature.argumentTypes))
		return JasminMethod(AccessModifier.Public, false, initSignature, instructions)
	}

	private fun buildMethod(classId: String, method: Method, needsScanner: Boolean): JasminMethod {
		val instructions = mutableListOf<Addressed<out ChangesStack<out StackChange>>>()
		val ctx = BodyContext(BodyAddress(classId, method.methodSignature.signature), method)
		val isMain = classId == Clazz.mainId && method.methodSignature.isMain
		ScopeBuilder(instructions::add, isMain, JasminValueType.create(method.methodSignature.returnType)).appendBody(ctx, needsScanner)
		val signature = if (isMain) {
			JasminSignature(method.methodSignature.signature.name, listOf(JasminReference.stringArray), JasminVoid)
		} else {
			JasminSignature(method.methodSignature)
		}
		return JasminMethod(method.methodSignature.accessModifier, isMain, signature, instructions)
	}
}