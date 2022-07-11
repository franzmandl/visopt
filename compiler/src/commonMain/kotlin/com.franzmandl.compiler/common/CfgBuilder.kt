package com.franzmandl.compiler.common

import com.franzmandl.compiler.ast.*

object CfgBuilder {
	fun build(compound: Compound): Cfg {
		val list = mutableListOf<MutableCfgNode>()
		fun createAndAddNode(id: Int): MutableCfgNode {
			val node = MutableCfgNode(id)
			list.add(node)
			return node
		}
		// Link successors.
		val entryNode = createAndAddNode(Cfg.entry)
		visitCompound(::createAndAddNode, Cfg.exit, false, compound)
		createAndAddNode(Cfg.exit)
		entryNode.naturalSuccessor = list.getOrNull(1)?.id ?: Cfg.exit
		// Link predecessors.
		val map = list.associateBy { it.id }
		for (node in list) {
			if (node.selfSuccessor) {
				node.complexPredecessors.add(node.id)
			}
			node.naturalSuccessor?.let { naturalSuccessor ->
				val successor = map[naturalSuccessor] ?: throw IllegalStateException("Node ${node.id}: Natural successor '$naturalSuccessor' not found.")
				if (successor.naturalPredecessor != null) {
					throw IllegalStateException("Node ${node.id}: Natural predecessor already set to '${successor.naturalPredecessor}'.")
				}
				successor.naturalPredecessor = node.id
			}
			node.complexSuccessor?.let { complexSuccessor ->
				(map[complexSuccessor] ?: throw IllegalStateException("Node ${node.id}: Complex successor '$complexSuccessor' not found.")).complexPredecessors.add(node.id)
			}
		}
		return Cfg(list.map { it.build() })
	}

	private fun visitCompound(createNode: (Int) -> MutableCfgNode, parentSuccessor: Int, isComplex: Boolean, compound: Compound) {
		for ((index, currentStatement) in compound.statements.withIndex()) {
			val possibleSuccessor = compound.statements.getOrNull(index + 1)?.id
			val node = createNode(currentStatement.id)
			when (currentStatement) {
				is BasicBlock -> {
					node.setSuccessor(possibleSuccessor, parentSuccessor, isComplex)
				}
				is IfStatement -> {
					val firstThenBranch = currentStatement.thenBranch.statements.firstOrNull()?.id
					val firstElseBranch = currentStatement.elseBranch?.statements?.firstOrNull()?.id
					if (firstThenBranch == null && firstElseBranch == null) {  // Both branches are empty.
						node.setSuccessor(possibleSuccessor, parentSuccessor, isComplex)
					} else if (firstElseBranch == null) {  // Else-branch is empty or has no else-branch.
						node.naturalSuccessor = firstThenBranch
						node.complexSuccessor = possibleSuccessor ?: parentSuccessor
						visitCompound(createNode, possibleSuccessor ?: parentSuccessor, possibleSuccessor == null && isComplex, currentStatement.thenBranch)
					} else if (firstThenBranch == null) {  // If-branch is empty.
						node.inverted = true
						node.naturalSuccessor = firstElseBranch
						node.complexSuccessor = possibleSuccessor ?: parentSuccessor
						visitCompound(createNode, possibleSuccessor ?: parentSuccessor, possibleSuccessor == null && isComplex, currentStatement.elseBranch)
					} else {  // Both branches are present.
						node.naturalSuccessor = firstThenBranch
						node.complexSuccessor = firstElseBranch
						visitCompound(createNode, possibleSuccessor ?: parentSuccessor, true, currentStatement.thenBranch)
						visitCompound(createNode, possibleSuccessor ?: parentSuccessor, possibleSuccessor == null && isComplex, currentStatement.elseBranch)
					}
				}
				is ReturnStatement -> {
					node.setSuccessor(null, Cfg.exit, parentSuccessor != Cfg.exit)
				}
				is WhileStatement -> {
					val firstBranch = currentStatement.branch.statements.firstOrNull()?.id
					if (firstBranch == null) {  // Branch is empty.
						if (possibleSuccessor == null && isComplex) {  // Has no natural successor.
							node.selfSuccessor = true
							node.complexSuccessor = parentSuccessor
						} else {
							node.inverted = true
							node.naturalSuccessor = possibleSuccessor ?: parentSuccessor
							node.complexSuccessor = currentStatement.id
						}
					} else {  // Branch is present.
						node.naturalSuccessor = firstBranch
						node.complexSuccessor = possibleSuccessor ?: parentSuccessor
						visitCompound(createNode, currentStatement.id, true, currentStatement.branch)
					}
				}
			}
		}
	}

	private class MutableCfgNode(val id: Int) {
		var naturalPredecessor: Int? = null
		val complexPredecessors = mutableSetOf<Int>()
		var naturalSuccessor: Int? = null
		var complexSuccessor: Int? = null
		var selfSuccessor = false
		var inverted = false

		fun setSuccessor(possibleSuccessor: Int?, parentSuccessor: Int, isComplex: Boolean) {
			if (possibleSuccessor != null) {
				naturalSuccessor = possibleSuccessor
			} else {
				if (isComplex) {
					complexSuccessor = parentSuccessor
				} else {
					naturalSuccessor = parentSuccessor
				}
			}
		}

		fun build() = CfgNode(id, naturalPredecessor, complexPredecessors, naturalSuccessor, complexSuccessor, selfSuccessor, inverted)
	}
}