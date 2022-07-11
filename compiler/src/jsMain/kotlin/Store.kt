import com.franzmandl.compiler.ast.*
import com.franzmandl.compiler.code.jasmin.ClassBuilder
import com.franzmandl.compiler.code.jasmin.InstructionFormatter
import com.franzmandl.compiler.code.jova.JovaFormatter
import com.franzmandl.compiler.common.LoopMode
import com.franzmandl.compiler.ctx.Address
import com.franzmandl.compiler.ctx.BodyAddress
import com.franzmandl.compiler.ctx.Command
import com.franzmandl.compiler.ctx.ProgramContext
import com.franzmandl.compiler.optimizer.Optimization
import com.franzmandl.compiler.optimizer.Optimizer
import kotlin.js.json

class Store(
	override var program: Program,
) : CommandCursor.Store {
	@JsName("getJasminCode")
	fun getJasminCode(
		tsAddress: TypeScript<BodyAddress>,
		tsActiveAddress: TypeScript<Address?>,
	) = Util.handleError(this::class, "getJasminCode", program, arrayOf(tsAddress)) {
		val address = Util.decodeFromTypeScript(tsAddress)
		val activeAddress = Util.decodeFromTypeScript(tsActiveAddress)
		val beforeBuilder = CodeBuilder("before")
		val activeBuilder = CodeBuilder("active")
		val afterBuilder = CodeBuilder("after")
		var currentBuilder = beforeBuilder
		val formatter = InstructionFormatter(currentBuilder.appendString)
		ProgramContext(program).mapBody(address) { ctx ->
			ClassBuilder.buildHasBodySymbol(address.classId, ctx.symbol, program.needsScanner).appendInstructions { instruction ->
				val (previousBuilders, nextBuilder) = when {
					activeAddress != null && activeAddress.contains(instruction.address) ->
						setOf(beforeBuilder) to activeBuilder
					activeBuilder.isEmpty() ->
						setOf<CodeBuilder>() to beforeBuilder
					else ->
						setOf(activeBuilder) to afterBuilder
				}
				if (currentBuilder != nextBuilder) {
					if (currentBuilder !in previousBuilders) {
						throw IllegalStateException("Illegal previous builder: " + currentBuilder.name)
					}
					currentBuilder = nextBuilder
					formatter.appendString = currentBuilder.appendString
				}
				formatter.appendInstruction(instruction)
			}
			ctx.original
		}
		json(
			beforeBuilder.name to beforeBuilder.build(),
			activeBuilder.name to activeBuilder.build(),
			afterBuilder.name to afterBuilder.build(),
		)
	}

	@JsName("getJovaCode")
	fun getJovaCode(
		tsAddress: TypeScript<Address>,
	) = Util.handleError(this::class, "getJovaCode", program, arrayOf(tsAddress)) {
		JovaFormatter.format(program, Util.decodeFromTypeScript(tsAddress))
	}

	@JsName("getVariables")
	fun getVariables(
		tsAddress: TypeScript<Address>,
	) = Util.handleError(this::class, "getVariables", program, arrayOf(tsAddress)) {
		val result = mutableListOf<Variable>()
		when (val address = Util.decodeFromTypeScript(tsAddress)) {
			is BodyAddress -> ProgramContext(program).mapBody(address) { ctx ->
				result.addAll(ctx.original.arguments)
				ctx.mapBasicStatements { ctx1 ->
					when (ctx1.original) {
						is Assignment, is ExpressionStatement, null -> {}
						is VariableDeclarations -> result.addAll(ctx1.original.variables)
					}
					ctx1.original
				}
				ctx.original
			}
			else -> throw IllegalStateException("Unsupported address type.")
		}
		Util.encodeToTypeScript(result)
	}

	@JsName("startCommandCursor")
	fun startCommandCursor(
		tsAddress: TypeScript<Address>,
		tsOptimizations: TypeScript<Set<Optimization>>,
		tsMode: TypeScript<LoopMode>,
		tsLiveOnExit: TypeScript<Set<Variable>>,
	) = Util.handleError(this::class, "startCommandCursor", program, arrayOf(tsAddress, tsOptimizations, tsMode, tsLiveOnExit)) {
		val commands = mutableListOf<Command>()
		val lastProgram = Optimizer.optimize(
			program,
			commands::add,
			Util.decodeFromTypeScript(tsMode),
			Util.decodeFromTypeScript(tsOptimizations),
			Util.decodeFromTypeScript(tsAddress),
			Util.decodeFromTypeScript(tsLiveOnExit),
		)
		var intermediateProgram = program
		for (command in commands) {
			intermediateProgram = command.apply(intermediateProgram)
		}
		if (intermediateProgram != lastProgram) {
			throw IllegalStateException("Commands invalid.")
		}
		json(
			"commandCursor" to CommandCursor(this, commands, lastProgram),
			"commands" to Util.encodeToTypeScript<List<Command>>(commands),
		)
	}

	@JsName("setProgram")
	fun setProgram(tsProgram: TypeScript<Program>) =
		Util.handleError(this::class, "setProgram", program, arrayOf(tsProgram)) {
			program = Util.decodeFromTypeScript(tsProgram)
		}

	private class CodeBuilder(val name: String) {
		private val stringBuilder = StringBuilder()

		val appendString = { value: String ->
			stringBuilder.append(value)
			Unit
		}

		fun build() = stringBuilder.toString().trim('\n')

		fun isEmpty() = stringBuilder.isEmpty()
	}
}