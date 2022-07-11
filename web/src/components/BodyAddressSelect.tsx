import {BodyAddress} from 'compiler-generated';
import {Dispatch} from 'react';
import {Input} from 'reactstrap';

export function BodyAddressSelect({
    bodyAddresses,
    htmlId,
    selected,
    setSelected,
}: {
    readonly bodyAddresses: ReadonlyArray<BodyAddress>;
    readonly htmlId: string;
    readonly selected?: BodyAddress;
    readonly setSelected: Dispatch<BodyAddress>;
}): JSX.Element {
    const selectedValue = selected !== undefined ? JSON.stringify(selected) : undefined;
    return (
        <Input id={htmlId} onChange={(ev) => setSelected(JSON.parse(ev.target.value))} type='select' value={selectedValue}>
            {bodyAddresses.map((bodyAddress) => {
                const value = JSON.stringify(bodyAddress);
                return (
                    <option key={value} value={value}>
                        {bodyAddress.classId}.{bodyAddress.signature.name}({bodyAddress.signature.argumentTypes.join(', ')})
                    </option>
                );
            })}
        </Input>
    );
}
