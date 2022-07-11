import {AppContext} from 'common/AppContext';
import {Store} from 'compiler';
import {Address, BodyAddress, CompoundStatementAddress, HasBodySymbol} from 'compiler-generated';
import {ClientState} from 'model/ClientReport';
import {Step} from 'model/Step';
import {ReactNode} from 'react';
import {getBasicBlock} from './ast/AstUtil';
import {renderBody} from './ast/BodyComponent';
import {FlowGraphComponent} from './FlowGraphComponent';
import {JasminCodeComponent} from './JasminCodeComponent';
import {ImmutableRefObject} from './ReactUtil';
import {CenterElement} from './useAutoCenterElement';

export enum CodeId {
    FlowGraph,
    Jova,
    Jasmin,
}

export function renderCode(
    codeId: CodeId,
    bodyAddress: BodyAddress,
    centerElement: CenterElement,
    context: AppContext,
    clientStateRef: ImmutableRefObject<ClientState>,
    onRenderError: (message: string, componentName: string, location: string) => void,
    step: Step | undefined,
    store: Store,
    symbol: HasBodySymbol
): ReactNode {
    let activeBlockAddress: CompoundStatementAddress | undefined;
    let activeStatementIndex: number | undefined;
    let activeExpressionIndices: ReadonlyArray<number> | undefined;
    let activeAddress: Address | undefined;
    let statementClassName: string | undefined;
    if (step !== undefined && (step.discriminator === 'BeforeStep' || step.discriminator === 'AfterStep')) {
        const before = step.discriminator === 'BeforeStep';
        const {command} = step;
        switch (command.discriminator) {
            case 'AddBasicStatement': {
                const {index, basicBlockAddress} = command.address;
                activeBlockAddress = basicBlockAddress.compoundStatementAddress;
                if (!before) {
                    activeAddress = {discriminator: 'BasicStatementAddress', ...command.address};
                    activeStatementIndex = index;
                }
                statementClassName = 'active-component-green';
                break;
            }
            case 'RemoveBasicStatement': {
                const {index, basicBlockAddress} = command.address;
                activeBlockAddress = basicBlockAddress.compoundStatementAddress;
                if (before) {
                    activeAddress = {discriminator: 'BasicStatementAddress', ...command.address};
                    activeStatementIndex = index;
                }
                statementClassName = 'active-component-red';
                break;
            }
            case 'ReplaceBasicStatement': {
                activeAddress = {discriminator: 'BasicStatementAddress', ...command.address};
                const {index, basicBlockAddress} = command.address;
                activeBlockAddress = basicBlockAddress.compoundStatementAddress;
                activeStatementIndex = index;
                statementClassName = 'active-component-blue';
                break;
            }
            case 'ReplaceCompoundStatement':
            case 'TakeBranch': {
                const compoundStatementAddress = command.address;
                activeBlockAddress = command.address;
                if (before) {
                    const index = getBasicBlock(symbol.body.compound, compoundStatementAddress)?.statements.length ?? -1;
                    activeAddress = {
                        discriminator: 'BasicStatementAddress',
                        basicBlockAddress: {compoundStatementAddress},
                        index,
                    };
                    activeStatementIndex = index;
                }
                statementClassName = 'active-component-red';
                break;
            }
            case 'ReplaceExpression': {
                activeAddress = {discriminator: 'ExpressionAddress', ...command.address};
                const {index, basicBlockAddress} = command.address.basicStatementAddress;
                activeBlockAddress = basicBlockAddress.compoundStatementAddress;
                activeStatementIndex = index;
                if (before && command.addStatement !== null) {
                    activeStatementIndex = command.addStatement.address.index;
                }
                activeExpressionIndices = command.address.indices;
                statementClassName = 'active-component-blue';
                break;
            }
        }
    }
    if (codeId === CodeId.FlowGraph) {
        return (
            <FlowGraphComponent
                activeBlockProps={{
                    activeBlockAddress,
                    activeStatement: {
                        activeExpressionIndices,
                        activeStatementIndex,
                        centerElement,
                        statementClassName,
                    },
                }}
                body={symbol.body}
                bodyAddress={bodyAddress}
                onRenderError={onRenderError}
            />
        );
    } else if (codeId === CodeId.Jova) {
        return renderBody(
            {activeBlockAddress, activeStatement: {activeExpressionIndices, activeStatementIndex, centerElement, statementClassName}},
            bodyAddress,
            symbol
        );
    } else if (codeId === CodeId.Jasmin) {
        return (
            <JasminCodeComponent
                activeAddress={activeAddress}
                bodyAddress={bodyAddress}
                centerElement={centerElement}
                context={context}
                clientStateRef={clientStateRef}
                statementClassName={statementClassName}
                store={store}
                triggerRerender={symbol}
            />
        );
    }
}
