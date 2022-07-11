package com.franzmandl.compiler.optimizer

import com.franzmandl.compiler.ast.Compound
import com.franzmandl.compiler.ast.Program
import com.franzmandl.compiler.ast.Variable
import com.franzmandl.compiler.common.LoopMode
import com.franzmandl.compiler.ctx.*

object Optimizer {
	fun optimize(firstProgram: Program, addCommand: AddCommand, mode: LoopMode, optimizations: Set<Optimization>, address: Address, liveOnExit: Set<Variable>): Program {
		val compoundOptimizations = if (Optimization.DeadCodeElimination in optimizations) {
			listOf<(CompoundContext) -> Compound>(
				{ ctx -> UnreachableCodeElimination.visitCompound(addCommand, ctx) },
				{ ctx -> DeadVariableElimination.visitCompound(addCommand, ctx) },
			)
		} else {
			listOf()
		}
		val sortedOptimizations = optimizations.sortedBy { it.order }
		var intermediateProgram = firstProgram
		mode.loopWhile {
			val previousProgram = intermediateProgram
			intermediateProgram = optimize(intermediateProgram, addCommand, sortedOptimizations, address, liveOnExit)
			for (compoundOptimization in compoundOptimizations) {
				val ctx = ProgramContext(intermediateProgram)
				intermediateProgram = when (address) {
					is ProgramAddress -> ctx.mapCompounds(compoundOptimization)
					is BodyAddress -> ctx.mapBody(address) { it.mapCompounds(compoundOptimization) }
					is CompoundAddress -> ctx.mapCompound(address, compoundOptimization)
					is CompoundStatementAddress, is BasicBlockAddress, is ExpressionBlockAddress, is BasicStatementAddress, is ExpressionAddress -> intermediateProgram
				}
			}
			previousProgram != intermediateProgram
		}
		return intermediateProgram
	}

	private fun optimize(program: Program, addCommand: AddCommand, sortedOptimizations: List<Optimization>, address: Address, liveOnExit: Set<Variable>): Program {
		val transformers = sortedOptimizations.map { optimization ->
			when (optimization) {
				Optimization.AlgebraicSimplifications -> AnyBlockTransformerExpressionReplaceVisitorProxy(AlgebraicSimplifications.createExpressionReplaceVisitor(addCommand))
				Optimization.CommonSubexpressionElimination -> CommonSubexpressionElimination.createAnyBlockTransformer(addCommand, liveOnExit)
				Optimization.ConstantFolding -> AnyBlockTransformerExpressionReplaceVisitorProxy(ConstantFolding.createExpressionReplaceVisitor(addCommand))
				Optimization.ConstantPropagation -> AnyPropagation.ConstantPropagation.createAnyBlockTransformer(addCommand, liveOnExit)
				Optimization.CopyPropagation -> AnyPropagation.CopyPropagation.createAnyBlockTransformer(addCommand, liveOnExit)
				Optimization.DeadCodeElimination -> DeadCodeElimination.createAnyBlockTransformer(addCommand, liveOnExit)
				Optimization.ReductionInStrength -> AnyBlockTransformerExpressionReplaceVisitorProxy(ReductionInStrength.createExpressionReplaceVisitor(addCommand))
				Optimization.ThreeAddressCode -> AnyBlockTransformerExpressionReplaceVisitorProxy(ThreeAddressCode.createExpressionReplaceVisitor(addCommand))
			}
		}
		val multiTransformer = AnyBasicBlockMultiTransformer(transformers)
		return when (address) {
			is ProgramAddress -> ProgramContext(program).mapAnyBasicBlocks(multiTransformer)
			is BodyAddress -> ProgramContext(program).mapBody(address) { it.mapAnyBasicBlocks(multiTransformer) }
			is CompoundAddress -> ProgramContext(program).mapCompound(address) { it.mapAnyBasicBlocks(multiTransformer) }
			is CompoundStatementAddress -> ProgramContext(program).mapCompoundStatement(address) { it.mapAnyBasicBlocks(multiTransformer) }
			is BasicBlockAddress -> ProgramContext(program).mapBasicBlock(address, multiTransformer::mapBasicBlock)
			is ExpressionBlockAddress -> ProgramContext(program).mapExpressionBlock(address, multiTransformer::mapExpressionBlock)
			is BasicStatementAddress, is ExpressionAddress -> throw IllegalStateException("Illegal address: $address")
		}
	}
}