import {Dispatch, useCallback, useEffect, useRef, useState} from 'react';
import {useLatest} from './useLatest';

export function useTask(task: () => boolean, interval: number): {readonly running: boolean; readonly setRunning: Dispatch<boolean>} {
    const [timeoutId, setTimeoutId] = useState<NodeJS.Timeout>();
    const start = useCallback(() => timeoutId === undefined && callbackRef.current(), [timeoutId]);
    const timeoutIdRef = useLatest(timeoutId);
    // Originally there should be a stopRef, but then react complains about
    // "The ref value stopRef.current will likely have changed by the time this effect cleanup function runs"
    // in the cleanup stop call.
    const stop = useCallback(() => {
        if (timeoutIdRef.current !== undefined) {
            clearTimeout(timeoutIdRef.current);
        }
        setTimeoutId(undefined);
    }, [timeoutIdRef]);
    const setRunning = useCallback((running: boolean) => (running ? start() : stop()), [start, stop]);
    useEffect(() => {
        return () => stop();
    }, [stop]);
    const callback = useCallback(async () => {
        setTimeoutId(task() ? setTimeout(() => callbackRef.current(), interval) : undefined);
    }, [task, interval]);
    // START Is basically useLatest, but if you would use useLatest, react compains about non-exhaustive dependencies.
    const callbackRef = useRef(callback);
    useEffect(() => {
        callbackRef.current = callback;
    }, [callback]);
    // END useLatest
    return {running: timeoutId !== undefined, setRunning};
}
