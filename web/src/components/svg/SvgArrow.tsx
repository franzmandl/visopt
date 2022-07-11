import classNames from 'classnames';
import './SvgArrow.scss';

export function SvgArrow({className, d}: {readonly className?: string; readonly d: string}): JSX.Element {
    return (
        <svg className={classNames(className, 'svg-arrow')}>
            <defs>
                <marker id='svg-arrow-marker' markerWidth='12' markerHeight='12' refX='6' refY='3' orient='auto'>
                    <path className='svg-arrow-head' d='M0,0 L6.5,3 L0,6 L1,3 Z'></path>
                </marker>
            </defs>
            <path className='svg-arrow-path' d={d}></path>
        </svg>
    );
}
