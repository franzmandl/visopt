import {allOptimizations, Optimization, optimizationAttributes} from 'model/Optimization';
import {Dispatch} from 'react';
import {FormGroupCheckbox} from './FormGroupCheckbox';

export function OptimizationSelect({
    selected,
    setSelected,
}: {
    readonly selected: ReadonlyArray<Optimization>;
    readonly setSelected: Dispatch<ReadonlyArray<Optimization>>;
}): JSX.Element {
    return (
        <>
            <FormGroupCheckbox
                checked={selected.length === allOptimizations.length}
                htmlId='optimizationSelectAll'
                setChecked={(checked) => setSelected(checked ? allOptimizations : [])}
            >
                All optimizations
            </FormGroupCheckbox>
            {Array.from(optimizationAttributes, ([optimization, {label}]) => (
                <FormGroupCheckbox
                    checked={selected.indexOf(optimization) !== -1}
                    htmlId={`optimizationSelect${optimization}`}
                    key={optimization}
                    setChecked={(checked) =>
                        setSelected(
                            checked
                                ? [...selected, optimization]
                                : selected.filter((selectedOptimization) => selectedOptimization !== optimization)
                        )
                    }
                >
                    {label}
                </FormGroupCheckbox>
            ))}
        </>
    );
}
