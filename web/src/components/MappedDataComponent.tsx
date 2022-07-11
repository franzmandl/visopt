import {useEffect, useState} from 'react';
import {useLatest} from './useLatest';

export type DataMapping<D> = Record<number, D | undefined>;

export function MappedDataComponent<D>({
    children,
    id,
    mapping,
    onIdError,
}: {
    readonly children: (data: D) => JSX.Element;
    readonly id: number;
    readonly mapping: DataMapping<D>;
    readonly onIdError: (id: number) => void;
}): JSX.Element {
    const [data, setData] = useState<D>();
    const onIdErrorRef = useLatest(onIdError);
    useEffect(() => {
        const nextData = mapping[id];
        if (nextData === undefined) {
            onIdErrorRef.current(id);
        }
        setData(nextData);
    }, [id, mapping, onIdErrorRef]);
    if (data === undefined) {
        return <></>;
    }
    return children(data);
}
