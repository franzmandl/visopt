import com.franzmandl.compiler.ast.Constructor
import com.franzmandl.compiler.ast.Member
import com.franzmandl.compiler.ast.Method
import com.franzmandl.compiler.ast.Program
import com.franzmandl.compiler.common.LoopMode
import com.franzmandl.compiler.ctx.BodyAddress
import kotlin.js.json

object Engine {
	val infiniteLoopMode = Util.encodeToTypeScript(LoopMode.infinite)
	val onceLoopMode = Util.encodeToTypeScript(LoopMode.once)

	@JsName("createStore")
	fun createStore(
		tsProgram: TypeScript<Program>,
	) = Util.handleError(this::class, "createStore", null, arrayOf(tsProgram)) {
		val program = Util.decodeFromTypeScript(tsProgram)
		json(
			"bodyAddresses" to Util.encodeToTypeScript(getBodyAddresses(program)),
			"initialProgram" to tsProgram,
			"store" to Store(program),
		)
	}

	@JsName("triggerError")
	fun triggerError() = Util.handleError(this::class, "triggerError", null, arrayOf()) {
		throw IllegalStateException("Example Engine Error")
	}

	private fun getBodyAddresses(program: Program): List<BodyAddress> {
		val bodyAddresses = mutableListOf<BodyAddress>()
		for (clazz in program.classes) {
			clazz.symbols.mapNotNullTo(bodyAddresses) { symbol ->
				when (symbol) {
					is Constructor -> if (symbol.constructorSignature.isDefault) {
						null
					} else {
						BodyAddress(clazz.id, symbol.constructorSignature.signature)
					}
					is Member -> null
					is Method -> BodyAddress(clazz.id, symbol.methodSignature.signature)
				}
			}
		}
		return bodyAddresses
	}
}