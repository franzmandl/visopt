import {Dispatch, ReactNode} from 'react';
import {FormGroup, Input} from 'reactstrap';

export function FormGroupCheckbox({
    checked,
    children,
    disabled,
    htmlId,
    setChecked,
}: {
    readonly checked: boolean;
    readonly children: ReactNode;
    readonly disabled?: boolean;
    readonly htmlId: string;
    readonly setChecked: Dispatch<boolean>;
}): JSX.Element {
    return (
        <FormGroup check>
            <Input checked={checked} disabled={disabled} id={htmlId} onChange={() => setChecked(!checked)} type='checkbox' />
            <label className='form-check-label' htmlFor={htmlId}>
                {children}
            </label>
        </FormGroup>
    );
}
