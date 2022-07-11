import {BasicBlock, CompoundStatement, ExpressionBlock} from 'compiler-generated';
import {ReactNode} from 'react';
import {renderExpression} from './ExpressionComponent';
import {renderBasicStatement} from './BasicStatementComponent';
import {ActiveStatementProps} from './ActiveStatementProps';
import {ActiveComponent} from 'components/ActiveComponent';
import classNames from 'classnames';

export function BasicBlockComponent({
    activeStatementProps,
    children,
    className,
    compoundStatement,
    expressionSuffix,
}: {
    readonly activeStatementProps: ActiveStatementProps;
    readonly children?: ReactNode;
    readonly className?: string;
    readonly compoundStatement: CompoundStatement;
    readonly expressionSuffix?: string;
}): JSX.Element {
    return (
        <div className={className}>
            {renderCompoundStatement(activeStatementProps, compoundStatement, expressionSuffix ?? '')}
            {children}
        </div>
    );
}

function renderCompoundStatement(props: ActiveStatementProps, compoundStatement: CompoundStatement, expressionSuffix: string): ReactNode {
    switch (compoundStatement.discriminator) {
        case 'BasicBlock':
            return renderBasicBlock(compoundStatement, props);
        case 'IfStatement':
            return renderExpressionBlock(compoundStatement.expressionBlock, 'if (', ')' + expressionSuffix, props);
        case 'ReturnStatement':
            return renderExpressionBlock(compoundStatement.expressionBlock, 'return ', ';', props);
        case 'WhileStatement':
            return renderExpressionBlock(compoundStatement.expressionBlock, 'while (', ')' + expressionSuffix, props);
    }
}

export function renderExpressionBlock(
    expressionBlock: ExpressionBlock,
    expressionPrefix: string,
    expressionSuffix: string,
    props: ActiveStatementProps
): ReactNode {
    const statementsLength = expressionBlock.basicBlock.statements.length;
    return (
        <>
            {renderBasicBlock(expressionBlock.basicBlock, props)}
            <ActiveComponent
                active={statementsLength === props.activeStatementIndex}
                centerElement={props.centerElement}
                className={classNames('code-line', props.statementClassName)}
            >
                {expressionPrefix}
                {renderExpression(
                    expressionBlock.expression,
                    statementsLength === props.activeStatementIndex ? props.activeExpressionIndices : undefined
                )}
                {expressionSuffix}
            </ActiveComponent>
        </>
    );
}

export function renderBasicBlock(basicBlock: BasicBlock, props: ActiveStatementProps): ReactNode {
    return basicBlock.statements.map((statement, index) => (
        <ActiveComponent
            key={index}
            active={index === props.activeStatementIndex}
            centerElement={props.centerElement}
            className={classNames('code-line', props.statementClassName)}
        >
            {renderBasicStatement(statement, index === props.activeStatementIndex ? props.activeExpressionIndices : undefined)}
        </ActiveComponent>
    ));
}
