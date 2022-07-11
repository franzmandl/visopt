import {CompoundStatementAddress} from 'compiler-generated';
import {CenterElement} from 'components/useAutoCenterElement';

export interface ActiveBlockProps {
    readonly activeBlockAddress: CompoundStatementAddress | undefined;
    readonly activeStatement: ActiveStatementProps;
}

export interface ActiveStatementProps {
    readonly activeStatementIndex?: number;
    readonly activeExpressionIndices?: ReadonlyArray<number>;
    readonly centerElement: CenterElement;
    readonly statementClassName?: string;
}

export function getActiveStatementProps(blockActive: boolean, activeStatement: ActiveStatementProps): ActiveStatementProps {
    const {activeExpressionIndices, activeStatementIndex, centerElement, statementClassName} = activeStatement;
    return {
        activeStatementIndex: blockActive ? activeStatementIndex : undefined,
        activeExpressionIndices: blockActive ? activeExpressionIndices : undefined,
        centerElement,
        statementClassName,
    };
}
