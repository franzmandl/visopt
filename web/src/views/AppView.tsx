import {Client} from 'common/Client';
import {CreateStore, CreateStoreResult} from 'compiler';
import {useCallback, useState} from 'react';
import {ProgramView} from './ProgramView';
import {UploadView} from './UploadView';
import {LoadingIndicator} from 'components/LoadingIndicator';
import {ModalComponent, ModalContent} from 'components/ModalComponent';
import {AppContext} from 'common/AppContext';
import {jovaString} from 'examples/all01';

export function AppView({client, createStore}: {readonly client: Client; readonly createStore: CreateStore}): JSX.Element {
    const [createStoreResult, setCreateStoreResult] = useState<CreateStoreResult>();
    const [defaultText, setDefaultText] = useState(jovaString);
    const [hasWarnings, setHasWarnings] = useState(false);
    const [loading, setLoading] = useState(false);
    const [modalContent, setModalContent] = useState<ModalContent>();
    const [context] = useState(() => new AppContext(client, setLoading, setModalContent));
    const clearStore = useCallback((nextDefaultText: string | undefined) => {
        if (nextDefaultText !== undefined) {
            setDefaultText(nextDefaultText);
        }
        setCreateStoreResult(undefined);
    }, []);
    return (
        <>
            {createStoreResult !== undefined && !hasWarnings ? (
                <ProgramView context={context} clearStore={clearStore} createStoreResult={createStoreResult} />
            ) : (
                <UploadView
                    context={context}
                    createStore={createStore}
                    defaultText={defaultText}
                    hasWarnings={hasWarnings}
                    setCreateStoreResult={setCreateStoreResult}
                    setDefaultText={setDefaultText}
                    setHasWarnings={setHasWarnings}
                />
            )}
            {loading && <LoadingIndicator />}
            <ModalComponent content={modalContent} />
        </>
    );
}
