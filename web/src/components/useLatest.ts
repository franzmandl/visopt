import {useEffect, useRef} from 'react';
import {ImmutableRefObject} from './ReactUtil';

export function useLatest<T>(value: T): Readonly<ImmutableRefObject<T>> {
    const valueRef = useRef(value);

    useEffect(() => {
        valueRef.current = value;
    }, [value]);

    return valueRef;
}
