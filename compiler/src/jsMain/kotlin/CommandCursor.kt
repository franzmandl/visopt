import com.franzmandl.compiler.ast.Program
import com.franzmandl.compiler.ctx.Command
import kotlin.js.json

/**
 * `position` points to the command which will be executed next.
 * 0 ... No command has been executed -> `store.program == firstProgram`.
 * 1 ... First command has been executed.
 * `commands.size` ... Last command has been executed -> `store.program == lastProgram`.
 */
class CommandCursor(
	private val store: Store,
	private val commands: List<Command>,
	private val lastProgram: Program,
) {
	interface Store {
		var program: Program
	}

	private val firstProgram = store.program
	private var position = 0

	@JsName("move")
	fun move(
		currentPosition: Int,
		updatedPosition: Int,
		tsProgram: TypeScript<Program>,
	) = Util.handleError(this::class, "move", store.program, arrayOf(currentPosition, updatedPosition, tsProgram)) { addWarning ->
		if (position != currentPosition) {
			addWarning("position != currentPosition: $position != $currentPosition")
			position = currentPosition
			store.program = Util.decodeFromTypeScript(tsProgram)
		}
		if (updatedPosition >= commands.size) {
			moveFastForward(updatedPosition)
		} else if (updatedPosition <= 0) {
			moveFastBackward(updatedPosition)
		} else if (updatedPosition == position - 1) {
			moveStepBackward()
		} else {
			if (updatedPosition < position) {
				moveFastBackward(0)
			}
			while (position < updatedPosition) {
				moveStepForward()
			}
		}
		json(
			"position" to position,
			"program" to Util.encodeToTypeScript(store.program),
		)
	}

	private fun moveFastBackward(minimumPosition: Int) {
		position = minimumPosition
		store.program = firstProgram
	}

	private fun moveFastForward(maximumPosition: Int) {
		position = maximumPosition
		store.program = lastProgram
	}

	private fun moveStepBackward() {
		position -= 1
		store.program = if (position <= 0) {
			firstProgram
		} else if (position >= commands.size) {
			lastProgram
		} else {
			val revertedProgram = commands[position].revert(store.program)
			if (revertedProgram != null) {
				revertedProgram
			} else {
				var intermediateProgram = firstProgram
				for (index in 0 until position) {
					intermediateProgram = commands[index].apply(intermediateProgram)
				}
				intermediateProgram
			}
		}
	}

	private fun moveStepForward() {
		store.program = if (position < 0) {
			firstProgram
		} else if (position >= commands.size) {
			lastProgram
		} else {
			commands[position].apply(store.program)
		}
		position += 1
	}
}