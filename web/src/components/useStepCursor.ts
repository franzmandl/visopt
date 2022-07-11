import {CommandCursor, StartCommandCursorResult} from 'compiler';
import {Command} from 'compiler-generated';
import {Step} from 'model/Step';
import {useMemo} from 'react';

export interface StepCursor {
    readonly commandCursor: CommandCursor;
    readonly maximumPosition: number;
    readonly steps: ReadonlyArray<Step>;
}

export function useStepCursor(startCommandCursorResult: StartCommandCursorResult | undefined): StepCursor | undefined {
    return useMemo(() => {
        if (startCommandCursorResult === undefined) {
            return;
        }
        const {commandCursor, commands} = startCommandCursorResult;
        const steps = createSteps(commands);
        return {
            commandCursor,
            maximumPosition: steps.length - 1,
            steps,
        };
    }, [startCommandCursorResult]);
}

function createSteps(commands: ReadonlyArray<Command>): ReadonlyArray<Step> {
    const steps: Step[] = [{discriminator: 'FirstStep', position: 0}];
    commands.forEach((command, index) => {
        steps.push({discriminator: 'BeforeStep', position: index, command});
        steps.push({discriminator: 'AfterStep', position: index + 1, command});
    });
    steps.push({discriminator: 'LastStep', position: commands.length});
    return steps;
}
