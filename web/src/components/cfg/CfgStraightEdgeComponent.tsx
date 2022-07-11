import {useEffect, useState} from 'react';
import {useResizeDetector} from 'react-resize-detector';
import {SvgArrow} from '../svg/SvgArrow';

export function CfgStraightEdgeComponent({index, showArrow}: {readonly index: number; readonly showArrow: boolean}): JSX.Element {
    const {height, width, ref} = useResizeDetector();
    const gridRowStart = index * 2 + 2;

    const [d, setD] = useState('M0,0 Z');

    useEffect(() => {
        setD(getEdgePath(width ?? 0, height ?? 0));
    }, [height, width]);

    return (
        <div className='cfg-edge-component' ref={ref} style={{gridColumn: 2, gridRowStart, gridRowEnd: gridRowStart + 1, height: '2rem'}}>
            {showArrow && <SvgArrow className='cfg-edge-component-0' d={d} />}
        </div>
    );
}

function getEdgePath(width: number, height: number): string {
    const d = [];
    d.push('M', width / 2, 0);
    d.push('L', width / 2, height);
    return d.join(' ');
}
