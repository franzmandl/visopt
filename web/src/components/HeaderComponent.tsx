import {MdiIcon} from './mdi/MdiIcon';
import {Button, ButtonGroup} from 'reactstrap';
import {mdiChevronDoubleLeft, mdiRedo, mdiUndo} from '@mdi/js';
import {History} from './useHistory';
import {AppContext} from 'common/AppContext';
import {ModalContent} from './ModalComponent';
import classNames from 'classnames';
import {useCallback} from 'react';

export function HeaderComponent({
    className,
    clearCursor,
    clearStore,
    context,
    hasCursor,
    history,
}: {
    readonly className?: string;
    readonly clearCursor: () => void;
    readonly clearStore: () => void;
    readonly context: AppContext;
    readonly hasCursor: boolean;
    readonly history: History;
}): JSX.Element {
    const redoLength = history.state.redoList.length;
    const undoLength = history.state.undoList.length;
    const onBack = useCallback(() => {
        if (redoLength === 0 && undoLength === 0) {
            clearStore();
        } else {
            context.setModalContent(getModalContent(clearStore));
        }
    }, [clearStore, context, redoLength, undoLength]);
    return (
        <div className={classNames('text-center', className)}>
            {hasCursor ? (
                <Button onClick={clearCursor}>
                    <MdiIcon path={mdiChevronDoubleLeft} />
                    Leave
                </Button>
            ) : (
                <ButtonGroup>
                    <Button onClick={onBack}>
                        <MdiIcon path={mdiChevronDoubleLeft} />
                        Back
                    </Button>
                    <Button disabled={undoLength === 0} onClick={history.undo}>
                        <MdiIcon path={mdiUndo} />
                        Undo
                    </Button>
                    <Button disabled={redoLength === 0} onClick={history.redo}>
                        <MdiIcon path={mdiRedo} />
                        Redo
                    </Button>
                </ButtonGroup>
            )}
        </div>
    );
}

function getModalContent(clearStore: () => void): ModalContent {
    return {
        header: 'Are you sure?',
        body: 'Lets you edit your optimized program. This cannot be undone.',
        renderFooter: (closeModal) => (
            <Button
                onClick={() => {
                    closeModal();
                    clearStore();
                }}
            >
                Ok
            </Button>
        ),
    };
}
