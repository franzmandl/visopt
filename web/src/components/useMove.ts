import {AppContext} from 'common/AppContext';
import {Program} from 'compiler-generated';
import {HistoryState} from 'components/useHistory';
import {Parameter} from 'model/Parameter';
import {Dispatch, useCallback} from 'react';
import {StepCursor} from './useStepCursor';

export type Move = (updatedPosition: number, componentName: string, location: string) => number | undefined;

export function useMove(
    context: AppContext,
    historyState: HistoryState,
    parameter: Parameter,
    program: Program,
    {commandCursor, steps}: StepCursor,
    setCommandPosition: Dispatch<number>,
    setProgram: Dispatch<Program>,
    setStepPosition: Dispatch<number>
): Move {
    const move = useCallback(
        (nextStepPosition: number, componentName: string, location: string) => {
            const step = steps[nextStepPosition];
            const result = context.handleEngineError(
                commandCursor.move(parameter.commandPosition, step.position, program),
                componentName,
                location,
                {
                    historyState,
                    parameter,
                }
            );
            if (result === undefined) {
                return;
            }
            setCommandPosition(result.position);
            setProgram(result.program);
            setStepPosition(nextStepPosition);
            return nextStepPosition;
        },
        [commandCursor, context, historyState, parameter, program, setCommandPosition, setProgram, setStepPosition, steps]
    );
    return move;
}
