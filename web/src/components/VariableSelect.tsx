import {Variable} from 'compiler-generated';
import {Dispatch} from 'react';
import {equalsVariable} from './ast/AstUtil';
import {FormGroupCheckbox} from './FormGroupCheckbox';

export function VariableSelect({
    allVariables,
    selected,
    setSelected,
}: {
    readonly allVariables: ReadonlyArray<Variable>;
    readonly selected: ReadonlyArray<Variable>;
    readonly setSelected: Dispatch<ReadonlyArray<Variable>>;
}): JSX.Element {
    return (
        <>
            <FormGroupCheckbox
                checked={allVariables.length !== 0 && selected.length === allVariables.length}
                htmlId='variableSelectAll'
                setChecked={(checked) => setSelected(checked ? allVariables : [])}
            >
                All variables
            </FormGroupCheckbox>
            {allVariables.map((variable) => (
                <FormGroupCheckbox
                    checked={selected.findIndex((selectedVariable) => equalsVariable(selectedVariable, variable)) !== -1}
                    htmlId={`variableSelect${variable.id}`}
                    key={variable.id}
                    setChecked={(checked) =>
                        setSelected(
                            checked
                                ? [...selected, variable]
                                : selected.filter((selectedVariable) => !equalsVariable(selectedVariable, variable))
                        )
                    }
                >
                    {variable.id}: {variable.type}
                </FormGroupCheckbox>
            ))}
        </>
    );
}
