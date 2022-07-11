import {BodyAddress, CompoundAddress, CompoundStatementAddress} from 'compiler-generated';
import {equalsSignature} from 'components/ast/AstUtil';

export function equalsBodyAddress(a: BodyAddress, b: BodyAddress): boolean {
    return a.classId === b.classId && equalsSignature(a.signature, b.signature);
}

export function equalsCompoundAddress(a: CompoundAddress, b: CompoundAddress): boolean {
    return (
        equalsBodyAddress(a.bodyAddress, b.bodyAddress) &&
        a.indices.length === b.indices.length &&
        a.indices.every((aIndex, index) => aIndex === b.indices[index])
    );
}

export function equalsCompoundStatementAddress(a: CompoundStatementAddress | undefined, b: CompoundStatementAddress | undefined): boolean {
    return a !== undefined && b !== undefined && equalsCompoundAddress(a.compoundAddress, b.compoundAddress) && a.index === b.index;
}
