import {Parameter} from 'model/Parameter';
import {useCallback, useMemo, useState} from 'react';

export function useHistory(
    presentParameter: Parameter,
    setPresentParameter: (presentParameter: Parameter, historyState: HistoryState) => void
): History {
    const [redoList, setRedoList] = useState<ReadonlyArray<Parameter>>([]);
    const [undoList, setUndoList] = useState<ReadonlyArray<Parameter>>([]);
    const state = useMemo<HistoryState>(() => ({redoList, undoList}), [redoList, undoList]);

    const append = useCallback(
        (nextParameter: Parameter) => {
            setRedoList([]);
            setPresentParameter(nextParameter, state);
            setUndoList((prevUndoList) => [presentParameter, ...prevUndoList]);
        },
        [state, presentParameter, setPresentParameter]
    );

    const redo = useCallback(() => {
        const [nextParameter, ...nextRedoList] = redoList;
        if (nextParameter !== undefined) {
            setRedoList(nextRedoList);
            setPresentParameter(nextParameter, state);
            setUndoList((prevUndoList) => [presentParameter, ...prevUndoList]);
        }
    }, [state, presentParameter, setPresentParameter, redoList]);

    const undo = useCallback(() => {
        const [nextParameter, ...nextUndoList] = undoList;
        if (nextParameter !== undefined) {
            setUndoList(nextUndoList);
            setPresentParameter(nextParameter, state);
            setRedoList((prevRedoList) => [presentParameter, ...prevRedoList]);
        }
    }, [state, presentParameter, setPresentParameter, undoList]);

    return useMemo(() => ({append, redo, state, undo}), [append, redo, state, undo]);
}

export interface HistoryState {
    readonly redoList: ReadonlyArray<Parameter>;
    readonly undoList: ReadonlyArray<Parameter>;
}

export interface History {
    readonly append: (nextParameter: Parameter) => void;
    readonly redo: () => void;
    readonly state: HistoryState;
    readonly undo: () => void;
}
