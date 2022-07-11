import {Body, BodyAddress, Compound, CompoundStatement, CompoundStatementAddress} from 'compiler-generated';
import {CfgComponent} from './cfg/CfgComponent';
import {BasicBlockComponent} from './ast/BasicBlockComponent';
import {useMemo} from 'react';
import {DataMapping, MappedDataComponent} from './MappedDataComponent';
import {ActiveBlockProps, getActiveStatementProps} from './ast/ActiveStatementProps';
import classNames from 'classnames';
import './FlowGraphComponent.scss';
import {equalsCompoundStatementAddress} from './ctx/AddressUtil';

interface AddressedCompoundStatement {
    address: CompoundStatementAddress;
    statement: CompoundStatement;
}

const componentName = 'FlowGraphComponent';
export function FlowGraphComponent({
    activeBlockProps,
    body,
    bodyAddress,
    onRenderError,
}: {
    readonly activeBlockProps: ActiveBlockProps;
    readonly body: Body;
    readonly bodyAddress: BodyAddress;
    readonly onRenderError: (message: string, componentName: string, location: string) => void;
}): JSX.Element {
    const compoundStatementMapping = useMemo(() => createCompoundStatementMapping(body, bodyAddress), [body, bodyAddress]);
    return (
        <CfgComponent
            cfg={body.cfg}
            className='flow-graph-component'
            onRenderError={onRenderError}
            renderContent={(id) => (
                <MappedDataComponent<AddressedCompoundStatement>
                    id={id}
                    mapping={compoundStatementMapping}
                    onIdError={(id: number) =>
                        onRenderError(
                            `Id "${id}" was not found in compound statement mapping: ${JSON.stringify(compoundStatementMapping)}`,
                            componentName,
                            'Anonymous renderContent'
                        )
                    }
                >
                    {(addressed: AddressedCompoundStatement) => renderBasicBlock(activeBlockProps, addressed)}
                </MappedDataComponent>
            )}
        />
    );
}

function renderBasicBlock(activeBlockProps: ActiveBlockProps, addressed: AddressedCompoundStatement): JSX.Element {
    const {activeBlockAddress, activeStatement} = activeBlockProps;
    const blockActive = equalsCompoundStatementAddress(activeBlockAddress, addressed.address);
    return (
        <BasicBlockComponent
            className={classNames('basic-block-component', {
                'basic-block-component-active': blockActive,
            })}
            activeStatementProps={getActiveStatementProps(blockActive, activeStatement)}
            compoundStatement={addressed.statement}
        />
    );
}

function createCompoundStatementMapping(body: Body, bodyAddress: BodyAddress): DataMapping<AddressedCompoundStatement> {
    const mapping: DataMapping<AddressedCompoundStatement> = {};
    createBasicBlockIdMappingHelper(bodyAddress, body.compound, [], mapping);
    return mapping;
}

function createBasicBlockIdMappingHelper(
    bodyAddress: BodyAddress,
    compound: Compound,
    indices: ReadonlyArray<number>,
    mapping: DataMapping<AddressedCompoundStatement>
): void {
    compound.statements.forEach((statement, index) => {
        switch (statement.discriminator) {
            case 'BasicBlock':
                mapping[statement.id] = {address: {compoundAddress: {bodyAddress, indices}, index}, statement};
                break;
            case 'IfStatement':
                mapping[statement.expressionBlock.basicBlock.id] = {address: {compoundAddress: {bodyAddress, indices}, index}, statement};
                createBasicBlockIdMappingHelper(bodyAddress, statement.thenBranch, [...indices, 0], mapping);
                if (statement.elseBranch !== null) {
                    createBasicBlockIdMappingHelper(bodyAddress, statement.elseBranch, [...indices, 1], mapping);
                }
                break;
            case 'ReturnStatement':
                mapping[statement.expressionBlock.basicBlock.id] = {address: {compoundAddress: {bodyAddress, indices}, index}, statement};
                break;
            case 'WhileStatement':
                mapping[statement.expressionBlock.basicBlock.id] = {address: {compoundAddress: {bodyAddress, indices}, index}, statement};
                createBasicBlockIdMappingHelper(bodyAddress, statement.branch, [...indices, 0], mapping);
                break;
        }
    });
}
