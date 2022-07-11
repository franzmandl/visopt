import classNames from 'classnames';
import {CenterElement} from 'components/useAutoCenterElement';
import {ReactNode, useEffect, useRef} from 'react';
import './ActiveComponent.scss';

export function ActiveComponent({
    active,
    children,
    centerElement,
    className,
}: {
    readonly active: boolean;
    readonly children: ReactNode;
    readonly centerElement: CenterElement;
    readonly className?: string;
}): JSX.Element {
    const ref = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (active) {
            centerElement(ref.current);
        }
    });

    return (
        <div className={classNames({'active-component': active}, className)} ref={ref}>
            {children}
        </div>
    );
}
