import classNames from 'classnames';
import {Command, PropagationReplaceExpressionReason, ReplaceExpression, Variable} from 'compiler-generated';
import {fromEngineOptimization, Optimization, optimizationAttributes} from 'model/Optimization';
import {ReactNode, useEffect, useState} from 'react';
import {equalsVariable} from './ast/AstUtil';
import {renderExpression} from './ast/ExpressionComponent';

export function ReasonComponent({before, command}: {readonly before: boolean; readonly command: Command}): JSX.Element {
    const [propagationReplaceExpressionReason, setPropagationReplaceExpressionReason] = useState<PropagationReplaceExpressionReason>();

    useEffect(() => {
        let nextPropagationReplaceExpressionReason = undefined;
        if (command.discriminator === 'ReplaceExpression') {
            if (command.reason.discriminator === 'PropagationReplaceExpressionReason') {
                nextPropagationReplaceExpressionReason = command.reason;
            }
        }
        setPropagationReplaceExpressionReason(nextPropagationReplaceExpressionReason);
    }, [command]);

    const optimization = fromEngineOptimization(command.optimization);
    const optimizationAttribute = optimizationAttributes.get(optimization);
    if (optimizationAttribute !== undefined) {
        return (
            <>
                {renderTransformationTitle(before, optimizationAttribute.label)}
                <hr />
                {renderReason(before, command)}
                {optimizationAttribute.mappingLabel !== undefined &&
                    propagationReplaceExpressionReason !== undefined &&
                    renderMapping(propagationReplaceExpressionReason, optimizationAttribute.mappingLabel)}
            </>
        );
    } else if (optimization === Optimization.ThreeAddressCode) {
        return (
            <>
                {renderTransformationTitle(before, 'Three Address Code')}
                <hr />
                {command.discriminator === 'ReplaceExpression' && command.reason.discriminator === 'VariableReplaceExpressionReason' && (
                    <table className={reasonTableClassName}>
                        <tbody>
                            <tr>
                                <td>{renderVerb(before, 'Add', 'ed')} variable</td>
                                <td>{renderCode(command.reason.variable.id, 'text-success')}</td>
                            </tr>
                            <tr>
                                <td>for expression</td>
                                <td>{renderCode(renderExpression(command.old, undefined), '')}</td>
                            </tr>
                        </tbody>
                    </table>
                )}
            </>
        );
    } else {
        return <></>;
    }
}

const reasonTableClassName = 'table table-sm mt-2';

function renderTransformationTitle(before: boolean, title: string): ReactNode {
    return (
        <div className='text-center'>
            {before ? 'Next' : 'Applied'} Transformation:
            <div className='fs-3'>{title}</div>
        </div>
    );
}

function renderReason(before: boolean, command: Command): ReactNode {
    switch (command.discriminator) {
        case 'AddBasicStatement':
            if (
                command.optimization === Optimization.CommonSubexpressionElimination &&
                command.toAdd.discriminator === 'Assignment' &&
                command.toAdd.lhs.discriminator === 'VariableAccess'
            ) {
                return (
                    <table className={reasonTableClassName}>
                        <tbody>
                            <tr>
                                <td>{renderVerb(before, 'Add', 'ed')} variable</td>
                                <td>{renderCode(command.toAdd.lhs.variable.id, 'text-success')}</td>
                            </tr>
                            <tr>
                                <td>for expression</td>
                                <td>{renderCode(renderExpression(command.toAdd.rhs, undefined), '')}</td>
                            </tr>
                        </tbody>
                    </table>
                );
            } else {
                return <>{renderVerb(before, 'Add', 'ed')} statement</>;
            }
        case 'RemoveBasicStatement':
            if (command.toRemove.discriminator === 'Assignment' && command.toRemove.lhs.discriminator === 'VariableAccess') {
                return renderRemoveAssignment(before, command.toRemove.lhs.variable, command.liveVariables, false);
            } else if (command.toRemove.discriminator === 'VariableDeclarations') {
                const oldIdsAndTypes = command.toRemove.variables.map(formatVariable);
                return removeUnusedVariables(before, oldIdsAndTypes);
            } else {
                return <>{renderVerb(before, 'Remove', 'd')} statement</>;
            }
        case 'ReplaceBasicStatement':
            if (command.old.discriminator === 'Assignment' && command.old.lhs.discriminator === 'VariableAccess') {
                return renderRemoveAssignment(before, command.old.lhs.variable, command.liveVariables, true);
            } else if (
                command.old.discriminator === 'VariableDeclarations' &&
                command.replacement.discriminator === 'VariableDeclarations'
            ) {
                const oldIdsAndTypes = command.old.variables.map(formatVariable);
                const newIdsAndTypes = command.replacement.variables.map(formatVariable);
                return removeUnusedVariables(
                    before,
                    oldIdsAndTypes.filter((idAndType) => newIdsAndTypes.indexOf(idAndType) === -1)
                );
            } else {
                return <>{renderVerb(before, 'Replace', 'd')} statement</>;
            }
        case 'ReplaceCompoundStatement':
        case 'TakeBranch':
            if (command.reason === 'return') {
                return <>{renderVerb(before, 'Remove', 'd')} unreachable statements.</>;
            } else if (command.reason === 'true' || command.reason === 'false') {
                return (
                    <table className={reasonTableClassName}>
                        <tbody>
                            <tr>
                                <td>{renderVerb(before, 'Remove', 'd')}</td>
                                <td>control statement</td>
                            </tr>
                            <tr>
                                <td>because</td>
                                <td>
                                    {'condition is '}
                                    <span className='font-monospace'>{command.reason}</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                );
            } else {
                return;
            }
        case 'ReplaceExpression':
            return (
                <table className={reasonTableClassName}>
                    <tbody>{renderReplaceExpressionReason(before, command)}</tbody>
                </table>
            );
    }
}

function renderRemoveAssignment(
    before: boolean,
    variable: Variable,
    liveVariables: ReadonlyArray<Variable>,
    withSideEffect: boolean
): ReactNode {
    return (
        <>
            <table className={reasonTableClassName}>
                <tbody>
                    <tr>
                        <td>{renderVerb(before, 'Remove', 'd')}</td>
                        <td>assignment</td>
                    </tr>
                    <tr>
                        <td>because</td>
                        <td>variable {renderCode(variable.id, 'text-danger')} is not live</td>
                    </tr>
                    {withSideEffect && (
                        <tr>
                            <td colSpan={2}>but it {before ? 'has' : 'had'} a side effect</td>
                        </tr>
                    )}
                </tbody>
            </table>
            Live variables at this line:
            {liveVariables.length === 0 ? (
                ' None'
            ) : (
                <ul>
                    {liveVariables.map((variable) => (
                        <li key={variable.id}>
                            {variable.id}: {variable.type}
                        </li>
                    ))}
                </ul>
            )}
        </>
    );
}

function removeUnusedVariables(before: boolean, removedIdsAndTypes: ReadonlyArray<string>): ReactNode {
    return (
        <>
            {renderVerb(before, 'Remove', 'd')} unused variables:
            <ul>
                {removedIdsAndTypes.map((idAndType) => (
                    <li key={idAndType}>{idAndType}</li>
                ))}
            </ul>
        </>
    );
}

function renderReplaceExpressionReason(before: boolean, {old, reason, replacement}: ReplaceExpression): ReactNode {
    switch (reason.discriminator) {
        case 'CoercionReplaceExpressionReason':
            return (
                <>
                    <tr>
                        <td>{renderVerb(before, 'Coerce', 'd')}</td>
                        <td>{renderCode(reason.old, 'text-danger')}</td>
                    </tr>
                    <tr>
                        <td>to</td>
                        <td>{renderCode(reason.replacement, 'text-success')}</td>
                    </tr>
                </>
            );
        case 'VariableReplaceExpressionReason':
            return (
                <>
                    <tr>
                        <td>{renderVerb(before, 'Replace', 'd')}</td>
                        <td>{renderCode(renderExpression(old, undefined), 'text-danger')}</td>
                    </tr>
                    <tr>
                        <td>with</td>
                        <td>{renderCode(reason.variable.id, 'text-success')}</td>
                    </tr>
                </>
            );
        case 'PropagationReplaceExpressionReason':
            return (
                <>
                    <tr>
                        <td>{renderVerb(before, 'Replace', 'd')}</td>
                        <td>{renderCode(reason.variable.id, 'text-danger')}</td>
                    </tr>
                    <tr>
                        <td>with</td>
                        <td>{renderCode(renderExpression(replacement, undefined), 'text-success')}</td>
                    </tr>
                    <tr>
                        <td>because</td>
                        <td>symbol was found in symbol table</td>
                    </tr>
                </>
            );
        case 'RuleReplaceExpressionReason':
            return (
                <>
                    <tr>
                        <td>{renderVerb(before, 'Replace', 'd')}</td>
                        <td>{renderCode(renderExpression(old, undefined), 'text-danger')}</td>
                    </tr>
                    <tr>
                        <td>with</td>
                        <td>{renderCode(renderExpression(replacement, undefined), 'text-success')}</td>
                    </tr>
                    <tr>
                        <td>because</td>
                        <td>{renderCode(reason.old + ' == ' + reason.replacement, '')}</td>
                    </tr>
                </>
            );
    }
}

function renderCode(code: ReactNode, className: string): ReactNode {
    return <span className={classNames('font-monospace', className)}>{code}</span>;
}

function renderVerb(before: boolean, verb: string, pastTenseSuffix: string): ReactNode {
    return (
        <>
            {verb}
            <span className={before ? 'opacity-0' : ''}>{pastTenseSuffix}</span>
        </>
    );
}

function renderMapping(reason: PropagationReplaceExpressionReason, label: string): ReactNode {
    return (
        <>
            Symbol table at this line:
            <table className='table table-bordered table-sm mt-2'>
                <thead>
                    <tr>
                        <th>Symbol</th>
                        <th>{label}</th>
                    </tr>
                </thead>
                <tbody className='border-top-0'>
                    {reason.mapping.map(({variable, expression}) => (
                        <tr
                            key={JSON.stringify(variable)}
                            className={classNames({
                                'table-success': equalsVariable(variable, reason.variable),
                            })}
                        >
                            <td>{variable.id}</td>
                            <td>{expression !== null ? renderExpression(expression, undefined) : <i>unset</i>}</td>
                        </tr>
                    ))}
                </tbody>
            </table>
        </>
    );
}

function formatVariable(variable: Variable): string {
    return variable.id + ': ' + variable.type;
}
