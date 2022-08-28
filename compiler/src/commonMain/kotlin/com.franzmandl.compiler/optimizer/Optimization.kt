package com.franzmandl.compiler.optimizer

import kotlinx.serialization.Serializable

// Add new optimizations here.
@Serializable
enum class Optimization(val order: Int) {
	AlgebraicSimplification(40),
	CommonSubexpressionElimination(70),
	ConstantFolding(30),
	ConstantPropagation(20),
	CopyPropagation(10),
	DeadCodeElimination(60),
	ReductionInStrength(50),
	ThreeAddressCode(0),
	;
}