import {AppContext} from 'common/AppContext';
import {JasminCode, Store} from 'compiler';
import {Address, BodyAddress} from 'compiler-generated';
import {ClientState} from 'model/ClientReport';
import {useCallback, useEffect, useState} from 'react';
import {ActiveComponent} from './ActiveComponent';
import {ImmutableRefObject} from './ReactUtil';
import {CenterElement} from './useAutoCenterElement';

const componentName = 'JasminCodeComponent';
export function JasminCodeComponent({
    activeAddress,
    bodyAddress,
    centerElement,
    context,
    clientStateRef,
    statementClassName,
    store,
    triggerRerender,
}: {
    activeAddress: Address | undefined;
    bodyAddress: BodyAddress;
    centerElement: CenterElement;
    context: AppContext;
    clientStateRef: ImmutableRefObject<ClientState>;
    statementClassName: string | undefined;
    store: Store;
    triggerRerender: unknown;
}): JSX.Element {
    const [jasminCode, setJasminCode] = useState<JasminCode>();

    const getJasminCode = useCallback(
        () =>
            context.handleEngineError(
                store.getJasminCode(bodyAddress, activeAddress ?? null),
                componentName,
                'Method getJasminCode',
                clientStateRef.current
            ),
        [activeAddress, bodyAddress, context, clientStateRef, store]
    );

    useEffect(() => {
        setJasminCode(getJasminCode());
    }, [getJasminCode, triggerRerender]);

    if (jasminCode === undefined) {
        return <></>;
    }
    const {before, active, after} = jasminCode;
    // prettier-ignore
    return <pre className='overflow-visible'>{before}<ActiveComponent active={active !== ''} centerElement={centerElement} className={statementClassName}>{active}</ActiveComponent>{after}</pre>;
}
