import classNames from 'classnames';
import {CfgNode} from 'compiler-generated';
import {MappedDataComponent} from 'components/MappedDataComponent';
import {ReactNode} from 'react';
import {Cfg} from './Cfg';
import {CfgCurvedEdgeComponent} from './CfgCurvedEdgeComponent';
import {CfgStraightEdgeComponent} from './CfgStraightEdgeComponent';

export type IndexMapping = Record<string, number | undefined>;

const componentName = 'CfgNodeComponent';
export function CfgNodeComponent({
    cfgNode: {id, naturalSuccessor, complexSuccessor, selfSuccessor, inverted},
    indexMapping,
    onRenderError,
    renderContent,
}: {
    readonly cfgNode: CfgNode;
    readonly indexMapping: IndexMapping;
    readonly onRenderError: (message: string, componentName: string, location: string) => void;
    readonly renderContent: (id: number) => ReactNode;
}): JSX.Element {
    const hasTwoSuccessors = complexSuccessor !== null && naturalSuccessor !== null;
    const specialLabel = id === Cfg.entry ? 'ENTRY' : id === Cfg.exit ? 'EXIT' : undefined;
    return (
        <MappedDataComponent<number>
            id={id}
            mapping={indexMapping}
            onIdError={(id: number) =>
                onRenderError(
                    `Node "${id}" was not found in index mapping: ${JSON.stringify(indexMapping)}`,
                    componentName,
                    'Child MappedDataComponent for index'
                )
            }
        >
            {(index: number) => (
                <>
                    <div style={{gridColumn: 2, gridRow: index * 2 + 1}}>
                        {specialLabel !== undefined ? renderSpecialContent(specialLabel) : renderContent(id)}
                    </div>
                    {complexSuccessor !== null && (
                        <MappedDataComponent<number>
                            id={complexSuccessor}
                            mapping={indexMapping}
                            onIdError={(id: number) =>
                                onRenderError(
                                    `Complex successor "${id}" was not found in index mapping: ${JSON.stringify(indexMapping)}`,
                                    componentName,
                                    'Child MappedDataComponent for complexSuccessorIndex'
                                )
                            }
                        >
                            {(complexIndex: number) => (
                                <>
                                    {hasTwoSuccessors && renderEdgeLabels(index, complexIndex, selfSuccessor, inverted)}
                                    <CfgCurvedEdgeComponent fromIndex={index} toIndex={complexIndex} />
                                </>
                            )}
                        </MappedDataComponent>
                    )}
                    {selfSuccessor && <CfgCurvedEdgeComponent fromIndex={index} toIndex={index} />}
                    {id !== Cfg.exit && <CfgStraightEdgeComponent index={index} showArrow={naturalSuccessor !== null && !selfSuccessor} />}
                </>
            )}
        </MappedDataComponent>
    );
}

function renderEdgeLabels(index: number, complexIndex: number, selfSuccessor: boolean, inverted: boolean): ReactNode {
    const gridRow = index * 2 + 2;
    const labelInverted = inverted !== selfSuccessor;
    return (
        <>
            <div className='position-relative' style={{gridColumn: 2, gridRow}}>
                <div className={classNames('position-absolute ps-1', {'end-0': complexIndex > index})}>{labelInverted ? 'T' : 'F'}</div>
            </div>
            <div className='position-relative' style={{gridColumn: selfSuccessor ? 1 : 2, gridRow}}>
                <div className={classNames('position-absolute ps-1 ', selfSuccessor ? 'end-0' : 'start-50')}>
                    {labelInverted ? 'F' : 'T'}
                </div>
            </div>
        </>
    );
}

function renderSpecialContent(label: string): ReactNode {
    return (
        <div className='text-center'>
            <div className='cfg-node-component'>{label}</div>
        </div>
    );
}
