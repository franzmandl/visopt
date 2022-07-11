import {Button, Form, FormGroup, Input, InputGroup, InputGroupText} from 'reactstrap';
import {MdiIcon} from './mdi/MdiIcon';
import {mdiSend} from '@mdi/js';
import {useCallback, useLayoutEffect, useState} from 'react';
import {wrapPreventDefault} from './ReactUtil';
import {Move} from './useMove';

const componentName = 'PositionComponent';
export function PositionComponent({
    maximumPosition,
    move,
    position,
    running,
}: {
    readonly maximumPosition: number;
    readonly move: Move;
    readonly position: number;
    readonly running: boolean;
}): JSX.Element {
    const positionString = position.toString();
    const [value, setValue] = useState(positionString);

    useLayoutEffect(() => setValue(positionString), [positionString]);

    const onFormSubmit = useCallback(() => {
        const parsedValue = Math.max(0, Math.min(maximumPosition, parseInt(value)));
        if (isNaN(parsedValue) || parsedValue.toString() === positionString) {
            setValue(positionString);
        } else {
            move(parsedValue, componentName, 'Method onFormSubmit');
        }
    }, [maximumPosition, move, positionString, value]);

    return (
        <Form onSubmit={wrapPreventDefault(onFormSubmit)}>
            <FormGroup>
                <InputGroup>
                    <InputGroupText>Step </InputGroupText>
                    <Input disabled={running} onChange={(ev) => setValue(ev.target.value)} value={value} />
                    <InputGroupText> of {maximumPosition}</InputGroupText>
                    {positionString !== value && (
                        <Button type='submit'>
                            <MdiIcon path={mdiSend} />
                        </Button>
                    )}
                </InputGroup>
            </FormGroup>
        </Form>
    );
}
