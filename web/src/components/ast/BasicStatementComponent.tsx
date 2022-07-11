import {BasicStatement} from 'compiler-generated';
import {ReactNode} from 'react';
import {renderExpression} from './ExpressionComponent';

export function renderBasicStatement(statement: BasicStatement, activeExpressionIndices: ReadonlyArray<number> | undefined): ReactNode {
    switch (statement.discriminator) {
        case 'Assignment':
            return (
                <span>
                    {renderExpression(statement.lhs, undefined)}
                    {' = '}
                    {renderExpression(statement.rhs, activeExpressionIndices)};
                </span>
            );
        case 'VariableDeclarations':
            return (
                <span>
                    {statement.type} {statement.variables.map((variable, index) => (index === 0 ? '' : ', ') + variable.id)};
                </span>
            );
        case 'ExpressionStatement':
            return <span>{renderExpression(statement.expression, activeExpressionIndices)};</span>;
    }
}
