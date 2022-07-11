import {BasicBlock, BodyAddress, Compound, CompoundStatementAddress, HasBodySymbol, Program, Signature, Variable} from 'compiler-generated';

export function findSymbol(program: Program, bodyAddress: BodyAddress): HasBodySymbol | undefined {
    const clazz = program.classes.find(({id}) => id === bodyAddress.classId);
    if (clazz === undefined) {
        return undefined;
    }
    const symbol = clazz.symbols.find(
        (symbol) =>
            (symbol.discriminator === 'Constructor' &&
                !symbol.constructorSignature.isDefault &&
                equalsSignature(symbol.constructorSignature.signature, bodyAddress.signature)) ||
            (symbol.discriminator === 'Method' && equalsSignature(symbol.methodSignature.signature, bodyAddress.signature))
    );
    if (symbol === undefined || symbol.discriminator === 'Member') {
        return undefined;
    }
    return symbol;
}

export function equalsSignature(a: Signature | null, b: Signature | null): boolean {
    return (
        a !== null &&
        b !== null &&
        a.name === b.name &&
        a.argumentTypes.length === b.argumentTypes.length &&
        a.argumentTypes.every((aType, index) => aType === b.argumentTypes[index])
    );
}

export function equalsVariable(a: Variable | null, b: Variable | null): boolean {
    return a !== null && b !== null && a.id === b.id && a.level === b.level && a.type === b.type;
}

export function getBasicBlock(compound: Compound | undefined, address: CompoundStatementAddress): BasicBlock | undefined {
    address.compoundAddress.indices.forEach((index) => {
        const compoundStatement = compound?.statements[index];
        switch (compoundStatement?.discriminator) {
            case 'IfStatement':
                if (index === 0) {
                    compound = compoundStatement.thenBranch;
                } else if (index === 1 && compoundStatement.elseBranch !== null) {
                    compound = compoundStatement.elseBranch;
                } else {
                    compound = undefined;
                }
                break;
            case 'WhileStatement':
                if (index === 0) {
                    compound = compoundStatement.branch;
                } else {
                    compound = undefined;
                }
                break;
            default:
                compound = undefined;
        }
    });
    const compoundStatement = compound?.statements[address.index];
    switch (compoundStatement?.discriminator) {
        case 'BasicBlock':
            return compoundStatement;
        case 'IfStatement':
            return compoundStatement.expressionBlock.basicBlock;
        case 'ReturnStatement':
            return compoundStatement.expressionBlock.basicBlock;
        case 'WhileStatement':
            return compoundStatement.expressionBlock.basicBlock;
    }
}
