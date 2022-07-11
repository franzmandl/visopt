import {useEffect, useState} from 'react';
import {useResizeDetector} from 'react-resize-detector';
import {SvgArrow} from '../svg/SvgArrow';

export function CfgCurvedEdgeComponent({fromIndex, toIndex}: {readonly fromIndex: number; readonly toIndex: number}): JSX.Element {
    const {height, width, ref} = useResizeDetector();
    const distance = toIndex - fromIndex;
    const margin = distance > 0 ? 2 : 0;
    const svgHeight = (height ?? 0) + margin;
    const svgWidth = (width ?? 0) + margin;
    const [d, setD] = useState('M0,0 Z');
    const gridStyleProps = getGridStyleProps(fromIndex, toIndex);

    useEffect(() => {
        setD(getEdgePath(svgHeight, svgWidth, distance));
    }, [distance, svgHeight, svgWidth]);

    return (
        <div className='cfg-edge-component cfg-curved-edge-component' ref={ref} style={gridStyleProps}>
            <SvgArrow className={`cfg-edge-component-${margin}`} d={d} />
        </div>
    );
}

function getGridStyleProps(fromIndex: number, toIndex: number): {gridColumn: number; gridRowStart: number; gridRowEnd: number} {
    if (toIndex <= fromIndex) {
        return {gridColumn: 1, gridRowStart: toIndex * 2 + 1, gridRowEnd: fromIndex * 2 + 2};
    } else {
        return {gridColumn: 3, gridRowStart: fromIndex * 2 + 2, gridRowEnd: toIndex * 2 + 1};
    }
}

interface Point {
    x: number;
    y: number;
}

function getDistanceCoefficient(width: number, yDistance: number): number {
    return Math.min(width, yDistance / 2);
}

function getLongEdgePoints(height: number, width: number): [Point, Point, Point, Point] {
    const startPoint = {x: 0, y: 0};
    const endPoint = {x: 0, y: height};
    const yDistance = height / 2;
    const controlPoint1 = {
        x: startPoint.x + getDistanceCoefficient(width, yDistance),
        y: startPoint.y + getDistanceCoefficient(width, yDistance),
    };
    const controlPoint2 = {
        x: endPoint.x + getDistanceCoefficient(width, yDistance),
        y: endPoint.y - getDistanceCoefficient(width, yDistance),
    };
    return [startPoint, controlPoint1, controlPoint2, endPoint];
}

function getEdgePath(height: number, width: number, distance: number): string {
    const d = [];
    const [startPoint, controlPoint1, controlPoint2, endPoint] = getLongEdgePoints(height, width);
    if (distance < 1) {
        startPoint.x = width - startPoint.x;
        startPoint.y = height - startPoint.y;
        controlPoint1.x = width - controlPoint1.x;
        controlPoint1.y = height - controlPoint1.y;
        controlPoint2.x = width - controlPoint2.x;
        controlPoint2.y = height - controlPoint2.y;
        endPoint.x = width - endPoint.x;
        endPoint.y = height - endPoint.y;
    }
    d.push('M', startPoint.x, startPoint.y);
    d.push('C', controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, endPoint.x, endPoint.y);
    return d.join(' ');
}
