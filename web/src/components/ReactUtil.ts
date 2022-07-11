import {BaseSyntheticEvent, MutableRefObject} from 'react';

export function preventDefault<T extends BaseSyntheticEvent>(ev: T): T {
    ev.preventDefault();
    return ev;
}

export function wrapPreventDefault<T extends BaseSyntheticEvent>(fn: (ev: T) => void): (ev: T) => void {
    return (ev) => fn(preventDefault(ev));
}

export type ImmutableRefObject<T> = Readonly<MutableRefObject<T>>;
