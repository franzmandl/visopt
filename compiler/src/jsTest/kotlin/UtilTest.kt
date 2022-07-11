import com.franzmandl.compiler.ctx.Address
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UtilTest {
	@Test
	fun test1() {
		assertEquals(Util.decodeFromTypeScript<Address?>(js("null")), null)
		assertEquals(Util.encodeToTypeScript<Address?>(null), js("null"))
	}

	@Serializable
	@SerialName("TestCompoundAddress")
	data class TestCompoundAddress(val indices: List<Int>) {
		@Required
		val head: Int? = if (indices.isNotEmpty()) indices[0] else null

		@Required
		val tail = indices.drop(1)
		//val tail get() = indices.drop(1)  // @Required does not work with computed properties
	}

	@Test
	fun test2() {
		assertEquals(TestCompoundAddress(listOf(10)).head, 10)
		assertEquals("{\"indices\":[],\"head\":null,\"tail\":[]}", Json.encodeToString(TestCompoundAddress(listOf())))
		assertEquals("{\"indices\":[15,16],\"head\":15,\"tail\":[16]}", Json.encodeToString(TestCompoundAddress(listOf(15, 16))))
		Util.encodeToTypeScript(TestCompoundAddress(listOf()))
		assertEquals(Util.decodeFromTypeScript(Util.encodeToTypeScript(TestCompoundAddress(listOf(20)))).head, 20)
	}
}