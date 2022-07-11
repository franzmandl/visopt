import {Program} from 'compiler-generated';
import {useTask} from './useTask';
import {Dispatch, ReactNode, useCallback} from 'react';
import {AppContext} from 'common/AppContext';
import {ControlButtonsComponent} from './ControlButtonsComponent';
import {PositionComponent} from './PositionComponent';
import {HistoryState} from './useHistory';
import {Parameter} from 'model/Parameter';
import {Input} from 'reactstrap';
import {useMove} from './useMove';
import {StepCursor} from './useStepCursor';

const componentName = 'ControlsComponent';
export function ControlsComponent({
    children,
    context,
    historyState,
    parameter,
    program,
    rawSpeed,
    stepCursor,
    stepPosition,
    setCommandPosition,
    setProgram,
    setRawSpeed,
    setStepPosition,
}: {
    readonly children: ReactNode;
    readonly context: AppContext;
    readonly historyState: HistoryState;
    readonly parameter: Parameter;
    readonly program: Program;
    readonly rawSpeed: number;
    readonly stepCursor: StepCursor;
    readonly stepPosition: number;
    readonly setCommandPosition: Dispatch<number>;
    readonly setProgram: Dispatch<Program>;
    readonly setRawSpeed: Dispatch<number>;
    readonly setStepPosition: Dispatch<number>;
}): JSX.Element {
    const move = useMove(context, historyState, parameter, program, stepCursor, setCommandPosition, setProgram, setStepPosition);
    const task = useCallback(() => {
        const result = move(stepPosition + 1, componentName, 'Method task');
        return result !== undefined && result < stepCursor.maximumPosition;
    }, [stepCursor.maximumPosition, move, stepPosition]);
    const interval = getIntervalFromRawSpeed(rawSpeed);
    const {running, setRunning} = useTask(task, interval);
    return (
        <>
            <PositionComponent maximumPosition={stepCursor.maximumPosition} move={move} position={stepPosition} running={running} />
            Speed: {1000 / interval} Steps/sec
            {renderSpeedSlider(rawSpeed, setRawSpeed)}
            {children}
            <hr />
            <div className='text-center'>
                <ControlButtonsComponent
                    maximumPosition={stepCursor.maximumPosition}
                    move={move}
                    position={stepPosition}
                    running={running}
                    setRunning={setRunning}
                />
            </div>
        </>
    );
}

function getIntervalFromRawSpeed(rawSpeed: number): number {
    if (rawSpeed === 0) {
        return 2000;
    } else {
        return 1000 / Math.abs(rawSpeed);
    }
}

function renderSpeedSlider(rawSpeed: number, setRawSpeed: Dispatch<number>): ReactNode {
    return <Input min={0} max={4} onChange={(ev) => setRawSpeed(parseInt(ev.target.value))} step={1} type='range' value={rawSpeed} />;
}
