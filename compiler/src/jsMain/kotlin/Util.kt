import com.franzmandl.compiler.ast.Program
import com.franzmandl.compiler.common.JsonFormat
import kotlin.js.Json
import kotlin.js.json
import kotlin.reflect.KClass

object Util {
	inline fun <reified T> decodeFromTypeScript(o: TypeScript<T>): T = JsonFormat.decodeFromString(JSON.stringify(o))
	inline fun <reified T> encodeToTypeScript(o: T): TypeScript<T> = JSON.parse(JsonFormat.encodeToString(o))

	fun <K : Any, T> handleError(kClass: KClass<K>, methodName: String, program: Program?, callArguments: Array<Any>, getPayload: ((String) -> Unit) -> T): Json {
		fun createContext() = json(
			"className" to kClass.simpleName,
			"methodName" to methodName,
			"program" to encodeToTypeScript(program),
			"callArguments" to callArguments,
		)

		val warnings = mutableListOf<String>()
		return try {
			val result = json(
				"discriminator" to "Success",
				"payload" to getPayload(warnings::add),
			)
			if (warnings.isNotEmpty()) {
				result["context"] = createContext()
				result["warnings"] = encodeToTypeScript(warnings)
			}
			result
		} catch (e: Throwable) {
			if (js("(e instanceof RangeError)") as Boolean) {
				json(
					"discriminator" to "RangeError"
				)
			} else {
				json(
					"discriminator" to "EngineError",
					"context" to createContext(),
					"error" to json(
						"type" to e::class.simpleName,
						"message" to e.message,
					),
					"warnings" to warnings,
				)
			}
		}
	}
}