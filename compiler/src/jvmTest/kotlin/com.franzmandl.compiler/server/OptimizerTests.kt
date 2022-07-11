package com.franzmandl.compiler.server

import com.franzmandl.compiler.Compiler
import com.franzmandl.compiler.ast.Type
import com.franzmandl.compiler.ast.Variable
import com.franzmandl.compiler.code.jova.JovaFormatter
import com.franzmandl.compiler.common.LoopMode
import com.franzmandl.compiler.ctx.*
import com.franzmandl.compiler.optimizer.Optimization
import com.franzmandl.compiler.optimizer.UnreachableCodeElimination
import com.franzmandl.compiler.suite.Util.all01addresses
import com.franzmandl.compiler.suite.Util.all01algebraicSimplificationsAddress
import com.franzmandl.compiler.suite.Util.all01allOptimizationsAddress
import com.franzmandl.compiler.suite.Util.all01commonSubexpressionEliminationAddress
import com.franzmandl.compiler.suite.Util.all01constantFoldingAddress
import com.franzmandl.compiler.suite.Util.all01constantPropagationAddress
import com.franzmandl.compiler.suite.Util.all01copyPropagationAddress
import com.franzmandl.compiler.suite.Util.all01deadCodeEliminationAddress
import com.franzmandl.compiler.suite.Util.all01fileName
import com.franzmandl.compiler.suite.Util.all01reductionInStrengthAddress
import com.franzmandl.compiler.suite.Util.all01unreachableCodeEliminationAddress
import com.franzmandl.compiler.suite.Util.assertCommandIntegrity
import com.franzmandl.compiler.suite.Util.dummyBodyAddress
import com.franzmandl.compiler.suite.Util.dummyClassId
import com.franzmandl.compiler.suite.Util.dummyCompoundAddress
import com.franzmandl.compiler.suite.Util.dummyFileName
import com.franzmandl.compiler.suite.Util.dummyMethodName
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OptimizerTests {
	private fun optimize(address: BodyAddress, liveOnExit: Set<Variable> = setOf()): String {
		val usedOptimizations = mutableSetOf<Optimization>()
		val optimizations = all01addresses[address]!!
		val commands = mutableListOf<Command>()
		fun addCommand(command: Command) {
			usedOptimizations.add(command.optimization)
			commands.add(command)
		}

		val serializationPhase = Compiler.fromFileName(all01fileName).checkTypes()
		val optimizerPhase = serializationPhase.optimize().optimize(::addCommand, LoopMode.once, optimizations, address, liveOnExit)
		assertCommandIntegrity(serializationPhase.program, optimizerPhase.program, commands)
		Assertions.assertThat(usedOptimizations).containsExactlyInAnyOrderElementsOf(optimizations)
		return optimizerPhase.formatJova(CompoundAddress(address, listOf()))
	}

	private fun createOptimizerPhase(input: String) =
		Compiler.fromString(dummyFileName, "class $dummyClassId { public int $dummyMethodName() { $input } }").checkTypes().optimize()

	private fun optimize(optimizations: Set<Optimization>, input: String, addCommand: AddCommand = {}, liveOnExit: Set<Variable> = setOf()) =
		createOptimizerPhase(input)
			.optimize(addCommand, LoopMode.once, optimizations, dummyBodyAddress, liveOnExit)
			.formatJova(dummyCompoundAddress)

	@Test
	fun testAllOptimizations() {
		Assertions.assertThat(optimize(all01allOptimizationsAddress)).isEqualToNormalizingWhitespace(
			"""
int input, result;
input = readInt() << 2;
tmp1 = input + 4;
result = tmp1 + tmp1;
return result;
"""
		)
	}

	@Test
	fun testAlgebraicSimplifications1() {
		Assertions.assertThat(optimize(all01algebraicSimplificationsAddress)).isEqualToNormalizingWhitespace(
			"""
int x;
x = 183;
x = x;
x = x;
x = x;
x = x;
return x;
"""
		)
	}


	@Test
	fun testAlgebraicSimplifications2() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.AlgebraicSimplifications), """
bool boolean, cond;
int lhs, rhs, result;
cond = true;
boolean = false;
lhs = 183;
rhs = 984;
result = +lhs;
result = -(-lhs);
result = 0 + rhs;
result = lhs + 0;
result = lhs + -rhs;
result = 0 - rhs;
result = lhs - 0;
result = lhs - -rhs;
result = 0 * rhs;
result = lhs * 0;
result = 1 * rhs;
result = lhs * 1;
result = -1 * rhs;
result = lhs * -1;
result = 0 / rhs;
result = lhs / 1;
result = lhs / -1;
result = 0 % rhs;
result = lhs % 1;
result = lhs % -1;
cond = !!boolean;
cond = false && boolean;
cond = boolean && false;
cond = true && boolean;
cond = boolean && true;
cond = true || boolean;
cond = boolean || true;
cond = false || boolean;
cond = boolean || false;
cond = cond ? boolean : false;
cond = cond ? true : boolean;
return result;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
bool boolean, cond;
int lhs, rhs, result;
cond = true;
boolean = false;
lhs = 183;
rhs = 984;
result = lhs;
result = lhs;
result = rhs;
result = lhs;
result = lhs - rhs;
result = -rhs;
result = lhs;
result = lhs + rhs;
result = 0;
result = 0;
result = rhs;
result = lhs;
result = -rhs;
result = -lhs;
result = 0;
result = lhs;
result = -lhs;
result = 0;
result = 0;
result = 0;
cond = boolean;
cond = false;
cond = false;
cond = boolean;
cond = boolean;
cond = true;
cond = true;
cond = boolean;
cond = boolean;
cond = cond && boolean;
cond = cond || boolean;
return result;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination1() {
		Assertions.assertThat(optimize(all01commonSubexpressionEliminationAddress)).isEqualToNormalizingWhitespace(
			"""
int a, b, result;
tmp1 = 3 * 4;
a = tmp1 / 2;
b = tmp1 * 2;
tmp2 = a + 10;
result = tmp2 * tmp2;
return result;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination2() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.CommonSubexpressionElimination),
				"""
int a, result;
a = 1;
result = (50 + 5 + a) * (50 + 5 + a);
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, result;
a = 1;
tmp1 = 50 + 5;
tmp2 = tmp1 + a;
result = tmp2 * tmp2;
return 0;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination3() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.CommonSubexpressionElimination),
				"""
int a, b, c, d;
a = 1;
b = 2;
c = 3;
d = 4;
a = b * c * d + b * c * d;
a = 1 * 7;
a = b * c * d + b * c * d;
b = 4;
a = b * c * d + b * c * d;
a = 2 * 7;
a = b * c * d + b * c * d;
c = 5;
a = b * c * d + b * c * d;
a = 3 * 7;
a = b * c * d + b * c * d;
d = 6;
a = b * c * d + b * c * d;
a = 4 * 7;
a = b * c * d + b * c * d;
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, b, c, d;
a = 1;
b = 2;
c = 3;
d = 4;
tmp1 = b * c;
tmp2 = tmp1 * d;
tmp3 = tmp2 + tmp2;
a = tmp3;
a = 1 * 7;
a = tmp3;
b = 4;
tmp4 = b * c;
tmp5 = tmp4 * d;
tmp6 = tmp5 + tmp5;
a = tmp6;
a = 2 * 7;
a = tmp6;
c = 5;
tmp7 = b * c;
tmp8 = tmp7 * d;
tmp9 = tmp8 + tmp8;
a = tmp9;
a = 3 * 7;
a = tmp9;
d = 6;
tmp10 = tmp7 * d;
tmp11 = tmp10 + tmp10;
a = tmp11;
a = 4 * 7;
a = tmp11;
return 0;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination4() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.CommonSubexpressionElimination),
				"""
int a, result;
a = 1;
result = (50 + 5 + a);
result = (50 + 5 + a);
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, result;
a = 1;
tmp1 = 50 + 5;
tmp2 = tmp1 + a;
result = tmp2;
result = tmp2;
return 0;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination5() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.CommonSubexpressionElimination),
				"""
int a, result;
a = 1;
result = (50 + 5 + a) + 2;
result = (50 + 5 + a) + 3;
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, result;
a = 1;
tmp1 = 50 + 5;
tmp2 = tmp1 + a;
result = tmp2 + 2;
result = tmp2 + 3;
return 0;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination6() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.CommonSubexpressionElimination),
				"""
int a, b, c;
a = 1;
b = 2;
c = 3;
a = b * c + b * c;
a = a / 1;
a = b * c + b * c;
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, b, c;
a = 1;
b = 2;
c = 3;
tmp1 = b * c;
tmp2 = tmp1 + tmp1;
a = tmp2;
a = a / 1;
a = tmp2;
return 0;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination7() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.CommonSubexpressionElimination),
				"""
int a, b, c;
a = 1;
b = 2;
c = 3;
tmp1 = b * c;
tmp2 = tmp1 * tmp1;
a = tmp2 * 5;
a = tmp2 * 5;
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, b, c;
a = 1;
b = 2;
c = 3;
tmp1 = b * c;
tmp2 = tmp1 * tmp1;
tmp3 = tmp2 * 5;
a = tmp3;
a = tmp3;
return 0;
"""
		)
	}

	@Test
	fun testCommonSubexpressionElimination8() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.CommonSubexpressionElimination),
				"""
int a, result;
a = 157;
result = (50 + 5) * (50 + 5) - (a * 4) + (50 + 5) * (a * 4) + 14;
return result;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, result;
a = 157;
tmp1 = 50 + 5;
tmp2 = a * 4;
result = tmp1 * tmp1 - tmp2 + tmp1 * tmp2 + 14;
return result;
"""
		)
	}

	@Test
	fun testConstantFolding1() {
		Assertions.assertThat(optimize(all01constantFoldingAddress)).isEqualToNormalizingWhitespace(
			"""
int result;
result = 10;
return result;
"""
		)
	}

	@Test
	fun testConstantPropagation1() {
		Assertions.assertThat(optimize(all01constantPropagationAddress)).isEqualToNormalizingWhitespace(
			"""
int i, result;
i = 2;
result = 2 * 2 + 2;
return result;
"""
		)
	}

	@Test
	fun testCopyPropagation1() {
		Assertions.assertThat(optimize(all01copyPropagationAddress)).isEqualToNormalizingWhitespace(
			"""
int i, copy, result;
i = readInt();
copy = i;
result = i * i + i;
return result;
"""
		)
	}

	@Test
	fun testDeadCodeElimination1() {
		Assertions.assertThat(optimize(all01deadCodeEliminationAddress)).isEqualToNormalizingWhitespace(
			"""
int a;
a = 201;
return 0;
"""
		)
	}

	@Test
	fun testUnreachableCodeElimination1() {
		Assertions.assertThat(optimize(all01unreachableCodeEliminationAddress)).isEqualToNormalizingWhitespace(
			"""
print("always true\n");
return 0;
"""
		)
	}

	@Test
	fun testDeadCodeElimination2() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.DeadCodeElimination),
				"""
int a, b, r;
a = 50;
b = 75;
if (true) {
	if (a != 0) {
		r = print("true");
	}
} else {
	r = print("false");
}
while (a > b) {
	r = print("while");
}
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a, b;
a = 50;
b = 75;
if (a != 0) {
	print("true");
}
while (a > b) {
	print("while");
}
return 0;
"""
		)
	}

	@Test
	fun testDeadCodeElimination3() {
		Assertions.assertThat(
			optimize(
				setOf(Optimization.DeadCodeElimination),
				"""
int a, b, c;
a = 2;
b = a * 73;
c = a / 0;
return 0;
"""
			)
		).isEqualToNormalizingWhitespace(
			"""
int a;
a = 2;
a / 0;
return 0;
"""
		)
	}

	@Test
	fun testReductionInStrength1() {
		Assertions.assertThat(optimize(all01reductionInStrengthAddress)).isEqualToNormalizingWhitespace(
			"""
int result;
result = (3 + 3) >> 2;
return result;
"""
		)
	}

	@Test
	fun testUnreachableCode1() {
		val firstProgram = createOptimizerPhase(
			"""
if (false) {
	print("true1");
}
if (true) {
	print("true2");
}
if (false) {
	print("true3");
} else {
	print("false3");
}
if (true) {
	print("true4");
} else {
	print("false4");
}
while (false) {
	print("while false");
}
while (true) {
	print("while true");
}
return 0;
"""
		).program
		val commands = mutableListOf<Command>()
		val lastProgram = ProgramContext(firstProgram).mapCompounds { UnreachableCodeElimination.visitCompound(commands::add, it) }
		Assertions.assertThat(JovaFormatter.format(lastProgram, dummyCompoundAddress)).isEqualToNormalizingWhitespace(
			"""
print("true2");
print("false3");
print("true4");
while (true) {
	print("while true");
}
return 0;
"""
		)
		assertCommandIntegrity(firstProgram, lastProgram, commands)
	}

	@Test
	fun testUnreachableCode2() {
		val firstProgram = createOptimizerPhase(
			"""
if (true) {
	print("true1");
	if(true) {
		print("true2");
	}
	print("true3");
} else {
	print("false1");
	if(true) {
		print("false2");
	}
	print("false3");
}
return 0;
"""
		).program
		val commands = mutableListOf<Command>()
		val lastProgram = ProgramContext(firstProgram).mapCompounds { UnreachableCodeElimination.visitCompound(commands::add, it) }
		Assertions.assertThat(JovaFormatter.format(lastProgram, dummyCompoundAddress)).isEqualToNormalizingWhitespace(
			"""
print("true1");
print("true2");
print("true3");
return 0;
"""
		)
		assertCommandIntegrity(firstProgram, lastProgram, commands)
	}

	@Test
	fun testUnreachableCode3() {
		val firstProgram = createOptimizerPhase(
			"""
if (true) {
	print("true1");
	if(true) {
		print("true2");
	}
	print("true3");
} else {
	print("false1");
	if(true) {
		print("false2");
	}
	print("false3");
}
while (false) {
	print("while false");
}
while (true) {
	print("while true");
}
return 0;
"""
		).program
		val commands = mutableListOf<Command>()
		val lastProgram = ProgramContext(firstProgram).mapCompounds { UnreachableCodeElimination.visitCompound(commands::add, it) }
		Assertions.assertThat(JovaFormatter.format(lastProgram, dummyCompoundAddress)).isEqualToNormalizingWhitespace(
			"""
print("true1");
print("true2");
print("true3");
while (true) {
	print("while true");
}
return 0;
"""
		)
		assertCommandIntegrity(firstProgram, lastProgram, commands)
	}

	@Test
	fun testConstantPropagationConstantFoldingDeadCodeElimination1() {
		val program0 = """
int a, b, c;
a = 21;
b = a / 3;
c = b + a;
return a * 2;
"""
		val program1ConstantPropagation = """
int a, b, c;
a = 21;
b = 21 / 3;
c = b + 21;
return 21 * 2;
"""
		val program2ConstantFolding = """
int a, b, c;
a = 21;
b = 7;
c = b + 21;
return 42;
"""
		val program3DeadCodeElimination = """
int b;
b = 7;
return 42;
"""
		Assertions.assertThat(
			optimize(setOf(Optimization.ConstantPropagation), program0)
		).isEqualToNormalizingWhitespace(program1ConstantPropagation)
		Assertions.assertThat(
			optimize(setOf(Optimization.ConstantFolding), program1ConstantPropagation)
		).isEqualToNormalizingWhitespace(program2ConstantFolding)
		Assertions.assertThat(
			optimize(setOf(Optimization.DeadCodeElimination), program2ConstantFolding, liveOnExit = setOf(Variable("b", 0, Type.int)))
		).isEqualToNormalizingWhitespace(program3DeadCodeElimination)
	}
}