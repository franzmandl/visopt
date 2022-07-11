import {ReactNode, useCallback, useEffect, useState} from 'react';
import {Button, Modal, ModalBody, ModalFooter, ModalHeader} from 'reactstrap';

export interface ModalContent {
    readonly header: ReactNode;
    readonly body: ReactNode;
    readonly renderFooter?: (closeModal: () => void) => ReactNode;
}

export function ModalComponent({content}: {readonly content?: ModalContent}): JSX.Element {
    const [open, setOpen] = useState(content !== undefined);
    useEffect(() => {
        setOpen(content !== undefined);
    }, [content]);
    const closeModal = useCallback(() => setOpen(false), []);
    const footer = content?.renderFooter?.(closeModal);
    return (
        <Modal isOpen={open}>
            <ModalHeader toggle={closeModal}>{content?.header}</ModalHeader>
            <ModalBody>{content?.body}</ModalBody>
            <ModalFooter>
                {footer}
                <Button onClick={closeModal}>{footer ? 'Cancel' : 'Ok'}</Button>
            </ModalFooter>
        </Modal>
    );
}
