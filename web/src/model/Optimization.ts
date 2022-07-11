import {Optimization as EngineOptimization} from 'compiler-generated';

// Add new Optimizations here.
export enum Optimization {
    AlgebraicSimplifications = 'AlgebraicSimplifications',
    CommonSubexpressionElimination = 'CommonSubexpressionElimination',
    ConstantFolding = 'ConstantFolding',
    ConstantPropagation = 'ConstantPropagation',
    CopyPropagation = 'CopyPropagation',
    DeadCodeElimination = 'DeadCodeElimination',
    ReductionInStrength = 'ReductionInStrength',
    ThreeAddressCode = 'ThreeAddressCode',
}

export const allOptimizations: ReadonlyArray<Optimization> = Object.values(Optimization).filter(
    (optimization) => optimization !== Optimization.ThreeAddressCode
);

export function fromEngineOptimization(optimization: EngineOptimization): Optimization {
    return optimization as Optimization;
}

export interface OptimizationAttribute {
    readonly label: string;
    readonly mappingLabel?: string;
}

// Add new Optimizations here.
export const optimizationAttributes: ReadonlyMap<Optimization, OptimizationAttribute> = new Map([
    [Optimization.AlgebraicSimplifications, {label: 'Algebraic Simplifications'}],
    [Optimization.CommonSubexpressionElimination, {label: 'Common Subexpression Elimination'}],
    [Optimization.ConstantFolding, {label: 'Constant Folding'}],
    [Optimization.ConstantPropagation, {label: 'Constant Propagation', mappingLabel: 'Value'}],
    [Optimization.CopyPropagation, {label: 'Copy Propagation', mappingLabel: 'Variable'}],
    [Optimization.DeadCodeElimination, {label: 'Dead Code Elimination'}],
    [Optimization.ReductionInStrength, {label: 'Reduction in Strength'}],
]);
