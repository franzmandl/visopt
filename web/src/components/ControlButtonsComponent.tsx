import {mdiChevronDoubleLeft, mdiChevronDoubleRight, mdiChevronLeft, mdiChevronRight, mdiPlay, mdiPause} from '@mdi/js';
import {Dispatch, useCallback, useEffect} from 'react';
import {MdiButton} from './mdi/MdiButton';
import {Move} from './useMove';

const componentName = 'ControlButtonsComponent';
export function ControlButtonsComponent({
    maximumPosition,
    move,
    position,
    running,
    setRunning,
}: {
    readonly maximumPosition: number;
    readonly move: Move;
    readonly position: number;
    readonly running: boolean;
    readonly setRunning: Dispatch<boolean>;
}): JSX.Element {
    const playPausePath = running ? mdiPause : mdiPlay;
    const playPauseTitle = running ? 'Pause' : 'Jump automatically';
    const moveFastBackward = useCallback(() => move(0, componentName, 'Method moveFastBackward'), [move]);
    const moveStepBackward = useCallback(() => move(position - 1, componentName, 'Method moveStepBackward'), [move, position]);
    const moveStepForward = useCallback(() => move(position + 1, componentName, 'Method moveStepForward'), [move, position]);
    const moveFastForward = useCallback(() => move(maximumPosition, componentName, 'Method moveFastForward'), [maximumPosition, move]);
    const keyDownListener = useCallback(
        (ev: KeyboardEvent) => {
            if (running) {
                return;
            }
            const target = ev.target as HTMLInputElement;
            if (target.tagName === 'INPUT' && target.type !== 'checkbox') {
                return;
            }
            if (ev.key === 'ArrowLeft' && position > 0) {
                moveStepBackward();
            } else if (ev.key === 'ArrowRight' && position < maximumPosition) {
                moveStepForward();
            }
        },
        [maximumPosition, moveStepBackward, moveStepForward, position, running]
    );
    useEffect(() => {
        document.addEventListener('keydown', keyDownListener);
        return () => {
            document.removeEventListener('keydown', keyDownListener);
        };
    }, [keyDownListener]);
    return (
        <>
            <MdiButton
                className='m-1'
                disabled={running || position <= 0}
                onClick={moveFastBackward}
                path={mdiChevronDoubleLeft}
                size='sm'
                title='First Step'
            />
            <MdiButton
                className='m-1'
                disabled={running || position <= 0}
                onClick={moveStepBackward}
                path={mdiChevronLeft}
                title='Previous Step'
            />
            <MdiButton
                className='m-1'
                disabled={position >= maximumPosition}
                onClick={() => setRunning(!running)}
                path={playPausePath}
                size='lg'
                title={playPauseTitle}
            />
            <MdiButton
                className='m-1'
                disabled={running || position >= maximumPosition}
                onClick={moveStepForward}
                path={mdiChevronRight}
                title='Next Step'
            />
            <MdiButton
                className='m-1'
                disabled={running || position >= maximumPosition}
                onClick={moveFastForward}
                path={mdiChevronDoubleRight}
                size='sm'
                title='Last Step'
            />
        </>
    );
}
