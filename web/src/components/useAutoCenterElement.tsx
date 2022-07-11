import {Dispatch, ReactNode, RefObject, useCallback, useEffect, useLayoutEffect, useRef, useState} from 'react';
import {FormGroupCheckbox} from './FormGroupCheckbox';

export type CenterElement = (element: HTMLElement | null) => void;

export interface AutoCenterElement {
    readonly centerElement: CenterElement;
    readonly enabled: boolean;
    /** Must be passed in onScroll property of element with scroll bar (=parentElement). */
    readonly onParentScroll: () => void;
    readonly setEnabled: Dispatch<boolean>;
    /** Must be placed at (0,0)-point of element with scroll bar (=parentElement). */
    readonly zeroElementRef: RefObject<HTMLDivElement>;
}

export function useAutoCenterElement(disableOnUserScroll: boolean): AutoCenterElement {
    const zeroElementRef = useRef<HTMLDivElement>(null);
    const duringAutoScroll = useRef(false);
    const [enabled, setEnabled] = useState(true);

    const onParentScroll = useCallback(() => {
        if (duringAutoScroll.current) {
            duringAutoScroll.current = false;
        } else if (disableOnUserScroll) {
            setEnabled(false);
        }
    }, [disableOnUserScroll]);

    // Sets duringAutoScroll.current to true during rerendering.
    useLayoutEffect(() => {
        duringAutoScroll.current = true;
    });
    useEffect(() => {
        duringAutoScroll.current = false;
    }, []);

    const centerElement = useCallback(
        (element: HTMLElement | null) => {
            const zeroElement = zeroElementRef.current;
            const parentElement = zeroElement?.parentElement ?? null;
            if (enabled && parentElement !== null && element !== null && zeroElement !== null) {
                const elementRect = element.getBoundingClientRect();
                const parentRect = parentElement.getBoundingClientRect();
                const zeroRect = zeroElement.getBoundingClientRect();
                const top = Math.max(0, elementRect.top - zeroRect.top - (parentRect.height - elementRect.height) / 2);
                duringAutoScroll.current = true;
                parentElement.scrollTo(0, top);
            }
        },
        [enabled]
    );

    return {centerElement, enabled, onParentScroll, setEnabled, zeroElementRef};
}

export function renderFollowCheckbox(autoCenterElement: AutoCenterElement, disabled: boolean): ReactNode {
    return (
        <>
            <FormGroupCheckbox
                checked={autoCenterElement.enabled}
                disabled={disabled}
                htmlId='follow'
                setChecked={autoCenterElement.setEnabled}
            >
                Automatically scroll to changes
            </FormGroupCheckbox>
        </>
    );
}
