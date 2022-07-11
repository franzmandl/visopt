import classNames from 'classnames';
import {BuiltinMethodInvocation, Expression} from 'compiler-generated';
import React, {ReactNode} from 'react';

export function renderExpression(expression: Expression, activeIndices: ReadonlyArray<number> | undefined): ReactNode {
    let head: number | undefined;
    let tail: ReadonlyArray<number> | undefined;
    if (activeIndices !== undefined) {
        [head, ...tail] = activeIndices;
    }
    const className = activeIndices?.length === 0 ? 'fw-bold' : '';
    switch (expression.discriminator) {
        case 'ArithmeticUnaryOperation':
            return (
                <span className={className}>
                    {expression.operator}
                    {wrapBraces(expression.needsBraces, renderExpression(expression.operand, head === 0 ? tail : undefined))}
                </span>
            );
        case 'LogicalNotUnaryOperation':
            return <span className={className}>!{renderExpression(expression.operand, head === 0 ? tail : undefined)}</span>;
        case 'ArithmeticBinaryOperation':
        case 'LogicalBinaryOperation':
        case 'RelationalBinaryOperation':
        case 'ObjectEqualsBinaryOperation':
            return (
                <span className={className}>
                    {wrapBraces(expression.needsLhsBraces, renderExpression(expression.operands.lhs, head === 0 ? tail : undefined))}
                    {' ' + expression.operator + ' '}
                    {wrapBraces(expression.needsRhsBraces, renderExpression(expression.operands.rhs, head === 1 ? tail : undefined))}
                </span>
            );
        case 'TernaryOperation':
            return (
                <span className={className}>
                    {wrapBraces(expression.needsConditionBraces, renderExpression(expression.condition, head === 0 ? tail : undefined))}
                    {' ? '}
                    {wrapBraces(expression.needsLhsBraces, renderExpression(expression.operands.lhs, head === 1 ? tail : undefined))}
                    {' : '}
                    {wrapBraces(expression.needsRhsBraces, renderExpression(expression.operands.rhs, head === 2 ? tail : undefined))}
                </span>
            );
        case 'CoercionExpression':
            return (
                <span className={className}>
                    {expression.operand.discriminator === 'CoercionExpression' && (
                        <span className='text-muted'>({expression.operand.expectedType})</span>
                    )}
                    {renderExpression(expression.operand, head === 0 ? tail : undefined)}
                </span>
            );
        case 'BooleanLiteralFalse':
            return <span className={className}>false</span>;
        case 'BooleanLiteralTrue':
            return <span className={className}>true</span>;
        case 'IntegerLiteral':
            return <span className={className}>{expression.value.toString()}</span>;
        case 'StringLiteral':
            return <span className={classNames(className, 'text-pre')}>{expression.value}</span>;
        case 'NixLiteral':
            return <span className={className}>nix</span>;
        case 'ThisExpression':
            return <span className={className}>this</span>;
        case 'VariableAccess':
            return <span className={className}>{expression.variable.id}</span>;
        case 'MemberAccess': {
            const active = head === 0;
            const showOperand = active || expression.operand.discriminator !== 'ThisExpression' || expression.operand.isExplicitly;
            return (
                <span className={className}>
                    {showOperand && <>{renderExpression(expression.operand, active ? tail : undefined)}.</>}
                    {expression.member.id}
                </span>
            );
        }
        case 'BuiltinMethodInvocation':
            return (
                <span className={className}>
                    {renderBuiltinMethodInvocationName(expression)}({renderArguments(expression.arguments, head, tail)})
                </span>
            );
        case 'MethodInvocation':
            return (
                <span className={className}>
                    {renderExpression(expression.operand, head === 0 ? tail : undefined)}.{expression.methodSignature.signature.name}(
                    {renderArguments(expression.arguments, head !== undefined ? head - 1 : undefined, tail)})
                </span>
            );
        case 'ObjectAllocation':
            return (
                <span className={className}>
                    new {expression.constructorSignature.signature.name}
                    {expression.constructorSignature.isDefault ? <></> : <>({renderArguments(expression.arguments, head, tail)})</>}
                </span>
            );
    }
}

function renderBuiltinMethodInvocationName(expression: BuiltinMethodInvocation): ReactNode {
    switch (expression.method.discriminator) {
        case 'PrintBoolMethod':
        case 'PrintIntMethod':
        case 'PrintStringMethod':
            return 'print';
        case 'ReadIntMethod':
            return 'readInt';
        case 'ReadStringMethod':
            return 'readString';
    }
}

function renderArguments(
    expressions: ReadonlyArray<Expression>,
    activeIndex: number | undefined,
    tail: ReadonlyArray<number> | undefined
): ReactNode {
    return expressions.map((expression, index) => {
        const result = (
            <React.Fragment key={index}>
                {index === 0 ? '' : ', '}
                {renderExpression(expression, activeIndex === index ? tail : undefined)}
            </React.Fragment>
        );
        return result;
    });
}

function wrapBraces(needsBraces: boolean, operand: ReactNode): ReactNode {
    return needsBraces ? <>({operand})</> : operand;
}
