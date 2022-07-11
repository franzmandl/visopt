package com.franzmandl.compiler.server

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.common.CfgBuilder
import com.franzmandl.compiler.suite.Util.createCompound
import com.franzmandl.compiler.suite.Util.createExpressionBlock
import com.franzmandl.compiler.suite.Util.dummyBasicStatement
import com.franzmandl.compiler.suite.Util.parseBody
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CfgTests {
	@Test
	fun test1() {
		val cfg = CfgBuilder.build(Compound(listOf()))
		Assertions.assertThat(cfg.list).isEqualTo(
			listOf(
				CfgNode(Cfg.entry, null, setOf(), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, Cfg.entry, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		)
	}

	@Test
	fun test2() {
		val cfg = CfgBuilder.build(Compound(listOf(ReturnStatement(createExpressionBlock(1, expression = IntegerLiteral.p0)))))
		Assertions.assertThat(cfg.list).isEqualTo(
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 1, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		)
	}

	@Test
	fun test3() {
		val cfg = CfgBuilder.build(
			Compound(
				listOf(
					IfStatement(createExpressionBlock(1, expression = BooleanLiteralTrue), Compound(listOf()), Compound(listOf())),
					ReturnStatement(createExpressionBlock(2, expression = IntegerLiteral.p0))
				)
			)
		)
		Assertions.assertThat(cfg.list).isEqualTo(
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 2, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		)
	}

	@Test
	fun test4() {
		val cfg = CfgBuilder.build(
			Compound(
				listOf(
					IfStatement(
						createExpressionBlock(1, expression = BooleanLiteralTrue),
						Compound(
							listOf(IfStatement(
								createExpressionBlock(2, expression = BooleanLiteralTrue),
								createCompound(3, basicStatement = dummyBasicStatement),
								createCompound(4, basicStatement = dummyBasicStatement)
							))
						), Compound(listOf())
					),
					ReturnStatement(createExpressionBlock(5, expression = IntegerLiteral.p0))
				)
			)
		)
		Assertions.assertThat(cfg.list).isEqualTo(
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 5, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), 3, 4, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), null, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, null, setOf(2), 5, null, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(1, 3), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 5, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		)
	}

	class TestCase(
		private val name: String,
		private val input: String,
		private val expectedList: List<CfgNode>,
	) {
		fun runTestCase() {
			println(name)
			val cfg = parseBody(input).cfg
			Assertions.assertThat(cfg.list).isEqualTo(expectedList)
		}

		override fun toString() = name
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource
	fun tests(testCase: TestCase) = testCase.runTestCase()
	fun tests() = listOf(
		TestCase(
			"bsp_2_1_backward(11,{d})",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  r = 3;" +
					"  while (r < 4) {" +
					"    r = 5; }" +
					"  if (r < 6) {" +
					"    r = 7; }" +
					"  r = 8; }" +
					"r = 9;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(8), 3, 9, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, null, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(5), 5, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(4), 7, 8, selfSuccessor = false, inverted = false),
				CfgNode(7, 6, setOf(), 8, null, selfSuccessor = false, inverted = false),
				CfgNode(8, 7, setOf(6), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(9, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 9, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"bsp_3b_i_backward(24,{a}) & bsp_3b_ii_backward(24,{b}) & bsp_3b_ii_forward(4)",
			"int r; r = 1; if (r < 1) {" +
					"  r = 2;" +
					"} else {" +
					"  r = 3; }" +
					"while (r < 4) {" +
					"  r = 5; }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 3, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(3, null, setOf(1), 4, null, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(2, 5), 5, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(4), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"edge_empty_else",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"    r = 4;" +
					"  } else {" +
					"  }" +
					"  r = 5; }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(5), 3, 6, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), 5, null, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(3), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"edge_empty_if",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"  } else {" +
					"    r = 4; }" +
					"  r = 5; }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(5), 3, 6, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 5, selfSuccessor = false, inverted = true),
				CfgNode(4, 3, setOf(), 5, null, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(3), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"edge_empty_while",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"}" +
					"r = 3;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(2), 3, 2, selfSuccessor = false, inverted = true),
				CfgNode(3, 2, setOf(), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 3, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"edge_nested_while_if_else1",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"    if (r < 4) {" +
					"      if (r < 5) {" +
					"        r = 6;" +
					"      } else {" +
					"        r = 7; }" +
					"    } else {" +
					"      r = 8; } " +
					"  } else {" +
					"    r = 9; } }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(6, 7, 8, 9), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 9, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), 5, 8, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), 6, 7, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(7, null, setOf(5), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(8, null, setOf(4), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(9, null, setOf(3), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, null, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"edge_nested_while_if_else2",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"    if (r < 4) {" +
					"      if (r < 5) {" +
					"        r = 6;" +
					"      } else {" +
					"        r = 7; }" +
					"    } else {" +
					"      r = 8; }" +
					"    r = 9;" +
					"  } else {" +
					"    r = 10; } }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(9, 10), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 10, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), 5, 8, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), 6, 7, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(), null, 9, selfSuccessor = false, inverted = false),
				CfgNode(7, null, setOf(5), null, 9, selfSuccessor = false, inverted = false),
				CfgNode(8, null, setOf(4), 9, null, selfSuccessor = false, inverted = false),
				CfgNode(9, 8, setOf(6, 7), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(10, null, setOf(3), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, null, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"edge_nested_while_if1",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"    if (r < 4) {" +
					"      if (r < 5) {" +
					"        r = 6; } } } }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(3, 4, 5, 6), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 2, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), 5, 2, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), 6, 2, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, null, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"edge_nested_while1",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  while (r < 3) {" +
					"    while (r < 4) {" +
					"      while (r < 5) {" +
					"        r = 6; } } } }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(3), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(4), 4, 2, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(5), 5, 3, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(6), 6, 4, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(), null, 5, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, null, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"example_star_backward(10,{b}) & example_star_forward(4)",
			"int r; r = 1; if (r < 1) {" +
					"  r = 2;" +
					"} else {" +
					"  r = 3; }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 3, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, null, setOf(1), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 3, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"example_without_braces",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"    r = 4;" +
					"  } else {" +
					"    r = 5; }" +
					"  r = 6; }" +
					"r = 7;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(6), 3, 7, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, null, setOf(3), 6, null, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(4), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(7, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 7, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"example",
			"int r; r = 1; if (r < 1) {" +
					"  r = 2;" +
					"} else {" +
					"  r = 3; }" +
					"while (r < 4) {" +
					"  r = 5; }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 3, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(3, null, setOf(1), 4, null, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(2, 5), 5, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(4), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"private1_backward(4,{a}) & private1_forward(4)",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"    r = 4; } }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(3, 4), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 2, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, null, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"private2_backward(75,{b,d})",
			"int r; r = 1; if (r < 1) {" +
					"  if (r < 2) {" +
					"    if (r < 3) {" +
					"      r = 4;" +
					"    } else {" +
					"      r = 5; }" +
					"    while (r < 6) {" +
					"      r = 7; }" +
					"    r = 8;" +
					"  } else {" +
					"    r = 9; }" +
					"  while (r < 10) {" +
					"    if (r < 11) {" +
					"      r = 12;" +
					"    } else {" +
					"      r = 13; }" +
					"    while (r < 14) {" +
					"      r = 15; }" +
					"    r = 16; }" +
					"  r = 17;" +
					"} else {" +
					"  r = 18; }" +
					"while (r < 19) {" +
					"  r = 20;" +
					"  while (r < 21) {" +
					"    r = 22; }" +
					"  r = 23; }" +
					"r = 24;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 18, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), 3, 9, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, null, setOf(3), 6, null, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(4, 7), 7, 8, selfSuccessor = false, inverted = false),
				CfgNode(7, 6, setOf(), null, 6, selfSuccessor = false, inverted = false),
				CfgNode(8, null, setOf(6), null, 10, selfSuccessor = false, inverted = false),
				CfgNode(9, null, setOf(2), 10, null, selfSuccessor = false, inverted = false),
				CfgNode(10, 9, setOf(8, 16), 11, 17, selfSuccessor = false, inverted = false),
				CfgNode(11, 10, setOf(), 12, 13, selfSuccessor = false, inverted = false),
				CfgNode(12, 11, setOf(), null, 14, selfSuccessor = false, inverted = false),
				CfgNode(13, null, setOf(11), 14, null, selfSuccessor = false, inverted = false),
				CfgNode(14, 13, setOf(12, 15), 15, 16, selfSuccessor = false, inverted = false),
				CfgNode(15, 14, setOf(), null, 14, selfSuccessor = false, inverted = false),
				CfgNode(16, null, setOf(14), null, 10, selfSuccessor = false, inverted = false),
				CfgNode(17, null, setOf(10), null, 19, selfSuccessor = false, inverted = false),
				CfgNode(18, null, setOf(1), 19, null, selfSuccessor = false, inverted = false),
				CfgNode(19, 18, setOf(17, 23), 20, 24, selfSuccessor = false, inverted = false),
				CfgNode(20, 19, setOf(), 21, null, selfSuccessor = false, inverted = false),
				CfgNode(21, 20, setOf(22), 22, 23, selfSuccessor = false, inverted = false),
				CfgNode(22, 21, setOf(), null, 21, selfSuccessor = false, inverted = false),
				CfgNode(23, null, setOf(21), null, 19, selfSuccessor = false, inverted = false),
				CfgNode(24, null, setOf(19), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 24, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"private3_backward(66,{c})",
			"int r; r = 1; if (r < 0) {" +
					"  r = 2;" +
					"} else {" +
					"  if (r < 3) {" +
					"    r = 4;" +
					"  } else {" +
					"    r = 5; }" +
					"  if (r < 6) {" +
					"    r = 7;" +
					"  } else {" +
					"    if (r < 8) {" +
					"      r = 9;" +
					"    } else {" +
					"      if (r < 10) {" +
					"        r = 11;" +
					"      } else {" +
					"        if (r < 12) {" +
					"          r = 13;" +
					"        } else {" +
					"          if (r < 14) {" +
					"            r = 15;" +
					"          } else {" +
					"            r = 16; } } } } } }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 3, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, null, setOf(1), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, null, setOf(3), 6, null, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(4), 7, 8, selfSuccessor = false, inverted = false),
				CfgNode(7, 6, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(8, null, setOf(6), 9, 10, selfSuccessor = false, inverted = false),
				CfgNode(9, 8, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(10, null, setOf(8), 11, 12, selfSuccessor = false, inverted = false),
				CfgNode(11, 10, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(12, null, setOf(10), 13, 14, selfSuccessor = false, inverted = false),
				CfgNode(13, 12, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(14, null, setOf(12), 15, 16, selfSuccessor = false, inverted = false),
				CfgNode(15, 14, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(16, null, setOf(14), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 16, setOf(2, 7, 9, 11, 13, 15), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"private4_backward(55,{i,z})",
			"int r; r = 1;" +
					"while(r < 2) {" +
					"  r = 3;" +
					"  while(r < 4) {" +
					"    if (r < 5) {" +
					"      if (r < 6) {" +
					"        if (r < 7) {" +
					"          r = 8; } } }" +
					"    r = 9; }" +
					"  if (r < 10) {" +
					"    r = 11;" +
					"  } else {" +
					"    if (r < 12) {" +
					"      r = 13;" +
					"    } else {" +
					"      if (r < 14) {" +
					"        r = 15;" +
					"      } else {" +
					"        r = 16; } } }" +
					"  r = 17; }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(17), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, null, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(9), 5, 10, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), 6, 9, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(), 7, 9, selfSuccessor = false, inverted = false),
				CfgNode(7, 6, setOf(), 8, 9, selfSuccessor = false, inverted = false),
				CfgNode(8, 7, setOf(), 9, null, selfSuccessor = false, inverted = false),
				CfgNode(9, 8, setOf(5, 6, 7), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(10, null, setOf(4), 11, 12, selfSuccessor = false, inverted = false),
				CfgNode(11, 10, setOf(), null, 17, selfSuccessor = false, inverted = false),
				CfgNode(12, null, setOf(10), 13, 14, selfSuccessor = false, inverted = false),
				CfgNode(13, 12, setOf(), null, 17, selfSuccessor = false, inverted = false),
				CfgNode(14, null, setOf(12), 15, 16, selfSuccessor = false, inverted = false),
				CfgNode(15, 14, setOf(), null, 17, selfSuccessor = false, inverted = false),
				CfgNode(16, null, setOf(14), 17, null, selfSuccessor = false, inverted = false),
				CfgNode(17, 16, setOf(11, 13, 15), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, null, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"private5_backward(85,{i,y})",
			"int r; r = 1; if (r < 1) {" +
					"  if (r < 2) {" +
					"    if (r < 3) {" +
					"      if (r < 4) {" +
					"        if (r < 5) {" +
					"          if (r < 6) {" +
					"            if (r < 7) {" +
					"              if (r < 8) {" +
					"                if (r < 9) {" +
					"                  if (r < 10) {" +
					"                    if (r < 11) {" +
					"                      if (r < 12) {" +
					"                        if (r < 13) {" +
					"                          if (r < 14) {" +
					"                            if (r < 15) {" +
					"                              if (r < 16) {" +
					"                                r = 17; } } } } } } } }" +
					"              } else {" +
					"                if (r < 18) {" +
					"                  if (r < 19) {" +
					"                    if (r < 20) {" +
					"                      if (r < 21) {" +
					"                        if (r < 22) {" +
					"                          if (r < 23) {" +
					"                            if (r < 24)  {" +
					"                              if (r < 25) {" +
					"                                r = 26; } } } } } } } } } } } } } } } }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), 5, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), 6, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(), 7, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(7, 6, setOf(), 8, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(8, 7, setOf(), 9, 18, selfSuccessor = false, inverted = false),
				CfgNode(9, 8, setOf(), 10, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(10, 9, setOf(), 11, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(11, 10, setOf(), 12, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(12, 11, setOf(), 13, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(13, 12, setOf(), 14, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(14, 13, setOf(), 15, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(15, 14, setOf(), 16, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(16, 15, setOf(), 17, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(17, 16, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(18, null, setOf(8), 19, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(19, 18, setOf(), 20, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(20, 19, setOf(), 21, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(21, 20, setOf(), 22, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(22, 21, setOf(), 23, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(23, 22, setOf(), 24, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(24, 23, setOf(), 25, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(25, 24, setOf(), 26, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(26, 25, setOf(), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(
					Cfg.exit,
					26,
					setOf(1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25),
					null,
					null,
					selfSuccessor = false,
					inverted = false
				),
			)
		),
		TestCase(
			"private6_backward(57,{y})",
			"int r; r = 1; if(r < 1) {" +
					"  r = 2;" +
					"} else {" +
					"  r = 3; }" +
					"while (r < 4) {" +
					"  if(r < 5) {" +
					"    if(r < 6) {" +
					"      r = 7;" +
					"    } else {" +
					"      r = 8; }" +
					"  } else {" +
					"    if(r < 9) {" +
					"      r = 10;" +
					"    } else {" +
					"      r = 11; } } }" +
					"if(r < 12) {" +
					"  r = 13;" +
					"} else {" +
					"  r = 14; }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 3, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(3, null, setOf(1), 4, null, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(2, 7, 8, 10, 11), 5, 12, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), 6, 9, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(), 7, 8, selfSuccessor = false, inverted = false),
				CfgNode(7, 6, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(8, null, setOf(6), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(9, null, setOf(5), 10, 11, selfSuccessor = false, inverted = false),
				CfgNode(10, 9, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(11, null, setOf(9), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(12, null, setOf(4), 13, 14, selfSuccessor = false, inverted = false),
				CfgNode(13, 12, setOf(), null, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(14, null, setOf(12), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 14, setOf(13), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"private8_backward(8,{a})",
			"int r; r = 1;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 1, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"s38ex4_backward(14,{i})",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  if (r < 3) {" +
					"    r = 4; }" +
					"  r = 5; }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(5), 3, 6, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), 5, null, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(3), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"test1_backward(12,{z})",
			"int r; r = 1; if (r < 1) {" +
					"  r = 2; }" +
					"r = 3;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 3, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), 3, null, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(1), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 3, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"test2_backward(17,{z})",
			"int r; r = 1;" +
					"while (r < 2) {" +
					"  r = 3;" +
					"  while (r < 4) {" +
					"    r = 5; } }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(4), 3, 6, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, null, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(5), 5, 2, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"test3_backward(17,{z})",
			"int r; r = 1; if (r < 1) {" +
					"  r = 2; }" +
					"while(r < 3) {" +
					"  r = 4; }" +
					"r = 5;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 3, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), 3, null, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(1, 4), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 3, selfSuccessor = false, inverted = false),
				CfgNode(5, null, setOf(3), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 5, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"test4_backward(16,{z}) & test4_forward(4)",
			"int r; r = 1; if (r < 1) {" +
					"  r = 2;" +
					"  while(r < 3) {" +
					"    r = 4; }" +
					"  r = 5; }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 6, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), 3, null, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(4), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 3, selfSuccessor = false, inverted = false),
				CfgNode(5, null, setOf(3), 6, null, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(1), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"test5_backward(16,{z}) & test5_forward(4)",
			"int r; r = 1;" +
					"while(r < 2) {" +
					"  if (r < 3) {" +
					"    r = 4; } }" +
					"r = 5;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(3, 4), 3, 5, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 2, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(5, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 5, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"test6_backward(20,{z}) & test6_forward(4)",
			"int r; r = 1; if (r < 1) {" +
					"  if (r < 2) {" +
					"    r = 3;" +
					"  } else {" +
					"    r = 4; }" +
					"  r = 5; }" +
					"r = 6;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, 6, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(), 3, 4, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), null, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, null, setOf(2), 5, null, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(3), 6, null, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(1), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 6, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"test7_backward(20,{z}) & test7_forward(4)",
			"int r; r = 1;" +
					"while(r < 2) {" +
					"  if (r < 3) {" +
					"    r = 4;" +
					"  } else {" +
					"    r = 5; }" +
					"  r = 6; }" +
					"r = 7;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(6), 3, 7, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, 5, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(), null, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, null, setOf(3), 6, null, selfSuccessor = false, inverted = false),
				CfgNode(6, 5, setOf(4), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(7, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 7, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"theoretical_fpa2_backward(5,{b}) & theoretical_fpa2_forward(5) & theoretical_fpa_backward(13,{d}) & theoretical_fpa_forward(4)",
			"int r; r = 1;" +
					"while(r < 2) {" +
					"  r = 3;" +
					"  while(r < 4) {" +
					"    r = 5; }" +
					"  if (r < 6) {" +
					"    r = 7; }" +
					"  r = 8; }" +
					"r = 9;",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(8), 3, 9, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(), 4, null, selfSuccessor = false, inverted = false),
				CfgNode(4, 3, setOf(5), 5, 6, selfSuccessor = false, inverted = false),
				CfgNode(5, 4, setOf(), null, 4, selfSuccessor = false, inverted = false),
				CfgNode(6, null, setOf(4), 7, 8, selfSuccessor = false, inverted = false),
				CfgNode(7, 6, setOf(), 8, null, selfSuccessor = false, inverted = false),
				CfgNode(8, 7, setOf(6), null, 2, selfSuccessor = false, inverted = false),
				CfgNode(9, null, setOf(2), Cfg.exit, null, selfSuccessor = false, inverted = false),
				CfgNode(Cfg.exit, 9, setOf(), null, null, selfSuccessor = false, inverted = false),
			)
		),
		TestCase(
			"Empty nested while body",
			"int r; r = 1;" +
					"while(r < 2) {" +
					"  while(r < 3) {" +
					"} }",
			listOf(
				CfgNode(Cfg.entry, null, setOf(), 1, null, selfSuccessor = false, inverted = false),
				CfgNode(1, Cfg.entry, setOf(), 2, null, selfSuccessor = false, inverted = false),
				CfgNode(2, 1, setOf(3), 3, Cfg.exit, selfSuccessor = false, inverted = false),
				CfgNode(3, 2, setOf(3), null, 2, selfSuccessor = true, inverted = false),
				CfgNode(Cfg.exit, null, setOf(2), null, null, selfSuccessor = false, inverted = false),
			)
		),
	)
}