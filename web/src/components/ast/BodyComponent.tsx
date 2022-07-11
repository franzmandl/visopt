import {BodyAddress, Compound, CompoundStatement, HasBodySymbol, Variable} from 'compiler-generated';
import {BasicBlockComponent} from 'components/ast/BasicBlockComponent';
import {equalsCompoundStatementAddress} from 'components/ctx/AddressUtil';
import {ReactNode} from 'react';
import {ActiveBlockProps, getActiveStatementProps} from './ActiveStatementProps';
import './BodyComponent.scss';

export function renderBody(activeBlockProps: ActiveBlockProps, bodyAddress: BodyAddress, symbol: HasBodySymbol): ReactNode {
    return (
        <div className='body-component'>
            <div className='code-line'>
                {renderSignature(symbol)} {'{'}
            </div>
            {renderCompound(activeBlockProps, symbol.body.compound, bodyAddress, [])}
            <div className='code-line'>{'}'}</div>
        </div>
    );
}

function renderSignature(symbol: HasBodySymbol) {
    const {arguments: variables} = symbol.body;
    switch (symbol.discriminator) {
        case 'Constructor': {
            const {signature} = symbol.constructorSignature;
            return (
                <span>
                    {signature.name}({renderArguments(variables)})
                </span>
            );
        }
        case 'Method': {
            const {accessModifier, returnType, signature} = symbol.methodSignature;
            return (
                <span>
                    {accessModifier} {returnType} {signature.name}({renderArguments(variables)})
                </span>
            );
        }
    }
}

function renderArguments(variables: ReadonlyArray<Variable>): ReactNode {
    return variables.map(renderVariable).join(', ');
}

function renderVariable(variable: Variable): ReactNode {
    return variable.type + ' ' + variable.id;
}

function renderCompound(
    activeBlockProps: ActiveBlockProps,
    compound: Compound,
    bodyAddress: BodyAddress,
    indices: ReadonlyArray<number>
): ReactNode {
    const {activeBlockAddress, activeStatement} = activeBlockProps;
    return (
        <div className='body-component-indent'>
            {compound.statements.map((statement, index) => (
                <div key={index}>
                    <BasicBlockComponent
                        activeStatementProps={getActiveStatementProps(
                            equalsCompoundStatementAddress({compoundAddress: {bodyAddress, indices}, index}, activeBlockAddress),
                            activeStatement
                        )}
                        compoundStatement={statement}
                        expressionSuffix=' {'
                    >
                        {renderCompoundStatementBranches(activeBlockProps, statement, bodyAddress, indices)}
                    </BasicBlockComponent>
                </div>
            ))}
        </div>
    );
}

function renderCompoundStatementBranches(
    activeBlockProps: ActiveBlockProps,
    compoundStatement: CompoundStatement,
    bodyAddress: BodyAddress,
    indices: ReadonlyArray<number>
): ReactNode {
    switch (compoundStatement.discriminator) {
        case 'IfStatement':
            return (
                <>
                    {renderCompound(activeBlockProps, compoundStatement.thenBranch, bodyAddress, [...indices, 0])}
                    {compoundStatement.elseBranch && (
                        <>
                            <div className='code-line'>{'} else {'}</div>
                            {renderCompound(activeBlockProps, compoundStatement.elseBranch, bodyAddress, [...indices, 1])}
                        </>
                    )}
                    <div className='code-line'>{'}'}</div>
                </>
            );
        case 'WhileStatement':
            return (
                <>
                    {renderCompound(activeBlockProps, compoundStatement.branch, bodyAddress, [...indices, 0])}
                    <div className='code-line'>{'}'}</div>
                </>
            );
    }
}
