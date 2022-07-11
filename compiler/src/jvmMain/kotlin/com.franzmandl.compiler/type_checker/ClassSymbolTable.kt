package com.franzmandl.compiler.type_checker

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.SignatureNullable
import com.franzmandl.compiler.common.SymbolTable
import java.util.*

class ClassSymbolTable(
	val type: Type,
) : HasId {
	private val constructorSignatures = SymbolTable<ConstructorSignature>()
	private val members = SymbolTable<Member>()
	private val methodSignatures = SymbolTable<MethodSignature>()
	private val symbols = LinkedList<ClassSymbol>()
	override val id = type.id

	fun addConstructor(constructor: Constructor) = symbols.add(constructor)

	fun addConstructorSignature(constructorSignature: ConstructorSignature) = constructorSignatures.add(constructorSignature)

	fun addMember(member: Member) = members.add(member) && symbols.add(member)

	fun addMethod(method: Method) = symbols.add(method)

	fun addMethodSignature(methodSignature: MethodSignature) = methodSignatures.add(methodSignature)

	fun getConstructorSignature(id: SignatureNullable) = constructorSignatures[id.toString()]

	fun getMember(id: String) = members[id]

	fun getMethodSignature(id: SignatureNullable) = methodSignatures[id.toString()]

	fun toAst() = Clazz(id, symbols)
}