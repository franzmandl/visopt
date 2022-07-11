package com.franzmandl.compiler.ctx

import com.franzmandl.compiler.ast.Signature
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
sealed class Address {
	abstract fun contains(other: Address): Boolean
}

@Serializable
@SerialName("ProgramAddress")
object ProgramAddress : Address() {
	override fun contains(other: Address) = true

	override fun toString() = "P"
}

@Serializable
@SerialName("BodyAddress")
data class BodyAddress(
	val classId: String,
	val signature: Signature,
) : Address() {
	override fun contains(other: Address) =
		when (other) {
			is BodyAddress -> other == this
			is CompoundAddress -> other.bodyAddress == this
			is CompoundStatementAddress -> other.compoundAddress.bodyAddress == this
			is BasicBlockAddress -> other.compoundStatementAddress.compoundAddress.bodyAddress == this
			is ExpressionBlockAddress -> other.compoundStatementAddress.compoundAddress.bodyAddress == this
			is BasicStatementAddress -> other.basicBlockAddress.compoundStatementAddress.compoundAddress.bodyAddress == this
			is ExpressionAddress -> other.basicStatementAddress.basicBlockAddress.compoundStatementAddress.compoundAddress.bodyAddress == this
			else -> false
		}

	override fun toString() = "${ProgramAddress}/B:$classId.$signature"
}

@Serializable
@SerialName("CompoundAddress")
data class CompoundAddress(
	val bodyAddress: BodyAddress,
	val indices: List<Int>,
) : Address() {
	@Transient
	val head = if (indices.isNotEmpty()) indices[0] else null
	val tail get(): CompoundAddress = CompoundAddress(bodyAddress, indices.drop(1))

	override fun contains(other: Address) =
		when (other) {
			is CompoundAddress -> other == this
			is CompoundStatementAddress -> other.compoundAddress == this
			is BasicBlockAddress -> other.compoundStatementAddress.compoundAddress == this
			is ExpressionBlockAddress -> other.compoundStatementAddress.compoundAddress == this
			is BasicStatementAddress -> other.basicBlockAddress.compoundStatementAddress.compoundAddress == this
			is ExpressionAddress -> other.basicStatementAddress.basicBlockAddress.compoundStatementAddress.compoundAddress == this
			else -> false
		}

	override fun toString() = "$bodyAddress/C:" + indices.joinToString(",")
}

@Serializable
@SerialName("CompoundStatementAddress")
data class CompoundStatementAddress(
	val compoundAddress: CompoundAddress,
	val index: Int,
) : Address() {
	override fun contains(other: Address) =
		when (other) {
			is CompoundStatementAddress -> other == this
			is BasicBlockAddress -> other.compoundStatementAddress == this
			is ExpressionBlockAddress -> other.compoundStatementAddress == this
			is BasicStatementAddress -> other.basicBlockAddress.compoundStatementAddress == this
			is ExpressionAddress -> other.basicStatementAddress.basicBlockAddress.compoundStatementAddress == this
			else -> false
		}

	override fun toString() = "$compoundAddress/CS:$index"
}

@Serializable
@SerialName("BasicBlockAddress")
data class BasicBlockAddress(
	val compoundStatementAddress: CompoundStatementAddress,
) : Address() {
	@Transient
	val index = compoundStatementAddress.index

	override fun contains(other: Address) =
		when (other) {
			is BasicBlockAddress -> other.compoundStatementAddress == compoundStatementAddress
			is BasicStatementAddress -> other.basicBlockAddress.compoundStatementAddress == compoundStatementAddress
			is ExpressionAddress -> other.basicStatementAddress.basicBlockAddress.compoundStatementAddress == compoundStatementAddress
			else -> false
		}

	override fun toString() = "$compoundStatementAddress/BB"
}

@Serializable
@SerialName("ExpressionBlockAddress")
data class ExpressionBlockAddress(
	val compoundStatementAddress: CompoundStatementAddress,
) : Address() {
	@Transient
	val index = compoundStatementAddress.index

	override fun contains(other: Address) =
		when (other) {
			is ExpressionBlockAddress -> other.compoundStatementAddress == compoundStatementAddress
			is BasicStatementAddress -> other.basicBlockAddress.compoundStatementAddress == compoundStatementAddress
			is ExpressionAddress -> other.basicStatementAddress.basicBlockAddress.compoundStatementAddress == compoundStatementAddress
			else -> false
		}

	override fun toString() = "$compoundStatementAddress/EB"
}

@Serializable
@SerialName("BasicStatementAddress")
data class BasicStatementAddress(
	val basicBlockAddress: BasicBlockAddress,
	val index: Int,
) : Address() {
	override fun contains(other: Address) =
		when (other) {
			is BasicStatementAddress -> other == this
			is ExpressionAddress -> other.basicStatementAddress == this
			else -> false
		}

	override fun toString() = "$basicBlockAddress/BS:$index"
}

@Serializable
@SerialName("ExpressionAddress")
data class ExpressionAddress(
	val basicStatementAddress: BasicStatementAddress,
	val rootIndex: Int,
	val indices: List<Int>,
) : Address() {
	@Transient
	val head = if (indices.isNotEmpty()) indices[0] else null
	val tail get(): ExpressionAddress = ExpressionAddress(basicStatementAddress, rootIndex, indices.drop(1))

	override fun contains(other: Address) =
		other is ExpressionAddress &&
				other.basicStatementAddress == basicStatementAddress &&
				other.rootIndex == rootIndex &&
				other.indices.size >= indices.size &&
				other.indices.slice(indices.indices) == indices

	override fun toString() = "$basicStatementAddress/E:" + rootIndex + "/" + indices.joinToString(",")
}