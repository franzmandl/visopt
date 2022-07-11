package com.franzmandl.compiler.server

import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.ctx.BodyAddress
import com.franzmandl.compiler.ctx.BodyContext
import com.franzmandl.compiler.fpa.FpaBuilder
import com.franzmandl.compiler.fpa.FpaNode
import com.franzmandl.compiler.suite.Util.parseBody
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FpaTests {
	@Test
	fun test1() {
		val a = Variable("a", 0, Type.int)
		val b = Variable("b", 0, Type.int)
		val n = Variable("n", 0, Type.int)
		val i = Variable("i", 0, Type.int)
		val signature = Signature("A", listOf())
		val fpa = FpaBuilder.build(
			BodyContext(
				BodyAddress(signature.name, signature), Constructor(
					ConstructorSignature(false, signature), parseBody(
						"""
        int a, b, n, i;
		// 1
		a = 1;
        b = 2;
        n = 3;
        i = a;
        if (a < i) {
			// 2
            n = n - 1;
        } else {
			// 3
            b = b + 4;
        }
		// 4
        while (i < n) {
			// 5
            b = n + 5;
            i = i + 1;
        }
		// 6
        a = n * i;
"""
					)
				)
			), setOf()
		)
		assertNode(fpa[Cfg.entry], setOf(), setOf(), setOf(), setOf())
		assertNode(fpa[1], setOf(a, b, n, i), setOf(), setOf(), setOf(b, i, n))
		assertNode(fpa[2], setOf(n), setOf(n), setOf(i, n), setOf(i, n))
		assertNode(fpa[3], setOf(b), setOf(b), setOf(b, i, n), setOf(i, n))
		assertNode(fpa[4], setOf(), setOf(i, n), setOf(i, n), setOf(i, n))
		assertNode(fpa[5], setOf(b, i), setOf(n, i), setOf(i, n), setOf(i, n))
		assertNode(fpa[6], setOf(a), setOf(n, i), setOf(n, i), setOf())
		assertNode(fpa[Cfg.exit], setOf(), setOf(), setOf(), setOf())
	}

	@Test
	fun test2() {
		val a = Variable("a", 0, Type.int)
		val b = Variable("b", 0, Type.int)
		val r = Variable("r", 0, Type.int)
		val signature = Signature("A", listOf())
		val fpa = FpaBuilder.build(
			BodyContext(
				BodyAddress(signature.name, signature), Constructor(
					ConstructorSignature(false, signature), parseBody(
						"""
        int a, b, r;
		// 1
        a = 50;
        b = 75;
        if (true) {
			// 2
            if (a != 0) {
				// 3
                r = print("true");
            }
        } else {
			// 4
            r = print("false");
        }
		// 5
        while (a > b) {
			// 6
            r = print("while");
        }
		// 7
		r = 0;
"""
					)
				)
			), setOf()
		)
		assertNode(fpa[Cfg.entry], setOf(), setOf(), setOf(), setOf())
		assertNode(fpa[1], setOf(a, b), setOf(), setOf(), setOf(a, b))
		assertNode(fpa[2], setOf(), setOf(a), setOf(a, b), setOf(a, b))
		assertNode(fpa[3], setOf(r), setOf(), setOf(a, b), setOf(a, b))
		assertNode(fpa[4], setOf(r), setOf(), setOf(a, b), setOf(a, b))
		assertNode(fpa[5], setOf(), setOf(a, b), setOf(a, b), setOf(a, b))
		assertNode(fpa[6], setOf(r), setOf(), setOf(a, b), setOf(a, b))
		assertNode(fpa[7], setOf(r), setOf(), setOf(), setOf())
		assertNode(fpa[Cfg.exit], setOf(), setOf(), setOf(), setOf())
	}

	private fun assertNode(fpaNode: FpaNode?, def: Set<Variable>, use: Set<Variable>, `in`: Set<Variable>, out: Set<Variable>) {
		Assertions.assertThat(fpaNode?.def).containsExactlyInAnyOrderElementsOf(def)
		Assertions.assertThat(fpaNode?.use).containsExactlyInAnyOrderElementsOf(use)
		Assertions.assertThat(fpaNode?.`in`).containsExactlyInAnyOrderElementsOf(`in`)
		Assertions.assertThat(fpaNode?.out).containsExactlyInAnyOrderElementsOf(out)
	}
}