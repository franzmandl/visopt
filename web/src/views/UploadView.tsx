import './UploadView.scss';
import {CreateStoreResult, CreateStore} from 'compiler';
import {Dispatch, ReactNode, useCallback, useEffect, useRef, useState} from 'react';
import {Button, Form} from 'reactstrap';
import {AppContext} from 'common/AppContext';
import {wrapPreventDefault} from 'components/ReactUtil';
import {useLatest} from 'components/useLatest';
import {renderPhaseMessage, renderPhasePrefix} from 'components/PhaseMessage';
import {Location, Phase, PhaseMessage} from 'compiler-generated';
import {instanceOfTypeCheckerError, TypeCheckerError} from 'model/TypeCheckerResult';
import {AxiosError} from 'axios';
import {instanceOfSpringError, SpringError} from 'model/SpringError';
import Editor from '@monaco-editor/react';
import {editor} from 'monaco-editor';
import classNames from 'classnames';

const componentName = 'UploadView';
export function UploadView({
    context,
    createStore,
    defaultText,
    hasWarnings,
    setCreateStoreResult,
    setDefaultText,
    setHasWarnings,
}: {
    readonly context: AppContext;
    readonly createStore: CreateStore;
    readonly defaultText: string;
    readonly hasWarnings: boolean;
    readonly setCreateStoreResult: Dispatch<CreateStoreResult | undefined>;
    readonly setDefaultText: Dispatch<string>;
    readonly setHasWarnings: Dispatch<boolean>;
}): JSX.Element {
    const [alertProps, setAlertProps] = useState<AlertProps>();
    const editorRef = useRef<editor.IStandaloneCodeEditor>();

    const onEditorMount = useCallback((editor: editor.IStandaloneCodeEditor) => {
        editorRef.current = editor;
    }, []);

    const setLocation = useCallback(({line, position}: Location) => {
        if (editorRef.current !== undefined) {
            editorRef.current.setPosition({lineNumber: line, column: position + 1});
            editorRef.current.revealLineInCenter(line);
            editorRef.current.focus();
        }
    }, []);

    const getTypeCheckerResult = useCallback(
        async (text: string) => {
            try {
                return await context.client.postTypeChecker(text);
            } catch (error) {
                const response = (error as AxiosError<SpringError | TypeCheckerError> | undefined)?.response;
                const status = response?.status;
                const data = response?.data;
                let nextAlertProps: AlertProps;
                if (status === 400 && instanceOfTypeCheckerError(data)) {
                    nextAlertProps = getPhaseAlertProps(data.phase, data.isWarning === true, data.messages, setLocation);
                } else {
                    let boldText = `Error ${status}`;
                    let normalText: string;
                    if (status === undefined) {
                        boldText = 'Network Error';
                        normalText = 'Server unreachable';
                    } else if (instanceOfSpringError(data)) {
                        normalText = data.error;
                    } else {
                        normalText = JSON.stringify(data, null, 2);
                    }
                    nextAlertProps = {
                        children: (
                            <>
                                <b>{boldText}:</b> {normalText}
                            </>
                        ),
                        color: 'danger',
                    };
                }
                setAlertProps(nextAlertProps);
                setCreateStoreResult(undefined);
            }
        },
        [context.client, setCreateStoreResult, setLocation]
    );

    const reformat = useCallback(async () => {
        context.load(async () => {
            const text = editorRef.current?.getValue() ?? defaultText;
            const result = await getTypeCheckerResult(text);
            if (result === undefined) {
                return;
            }
            const {program} = result;
            const store = context.handleEngineError(createStore(program), componentName, 'Method reformat', {
                text,
                typeCheckerResult: result,
            })?.store;
            if (store === undefined) {
                return;
            }
            const nextDefaultText = context.handleEngineError(
                store.getJovaCode({discriminator: 'ProgramAddress'}),
                componentName,
                'Method reformat',
                {
                    text,
                    typeCheckerResult: result,
                }
            );
            if (nextDefaultText === undefined) {
                return;
            }
            setDefaultText(nextDefaultText);
            editorRef.current?.setValue(nextDefaultText);
        });
    }, [context, createStore, defaultText, getTypeCheckerResult, setDefaultText]);

    const onFormSubmit = useCallback(async () => {
        if (hasWarnings) {
            setHasWarnings(false);
            return;
        }
        context.load(async () => {
            const text = editorRef.current?.getValue() ?? defaultText;
            const result = await getTypeCheckerResult(text);
            if (result === undefined) {
                return;
            }
            const {program, typeWarnings} = result;
            setHasWarnings(typeWarnings.length !== 0);
            setAlertProps(getPhaseAlertProps('TypeChecker', true, typeWarnings, setLocation));
            let nextCreateStoreResult = context.handleEngineError(createStore(program), componentName, 'Method onFormSubmit', {
                text,
                typeCheckerResult: result,
            });
            if (nextCreateStoreResult?.bodyAddresses.length === 0) {
                nextCreateStoreResult = undefined;
                setAlertProps({
                    children: 'Please add at least one method.',
                    color: 'danger',
                });
            }
            setCreateStoreResult(nextCreateStoreResult);
        });
    }, [hasWarnings, context, setHasWarnings, defaultText, getTypeCheckerResult, setLocation, createStore, setCreateStoreResult]);

    const onFormSubmitRef = useLatest(onFormSubmit);

    useEffect(() => {
        if (context.paramBodyIndex !== null) {
            onFormSubmitRef.current();
        }
    }, [context.paramBodyIndex, onFormSubmitRef]);

    const className = alertProps !== undefined ? `alert-${alertProps.color}` : 'bg-light';

    return (
        <Form className='upload-view d-grid h-100' onSubmit={wrapPreventDefault(onFormSubmit)}>
            <div className='upload-view-editor border-end' style={{gridColumn: 1, gridRow: '1 / 4'}}>
                <Editor defaultValue={defaultText} onMount={onEditorMount} />
            </div>
            <div className='p-1 mb-2' style={{gridColumn: 2, gridRow: 1}}>
                <div className='fs-1 text-center'>VisOpt</div>
                <div className='fs-3 text-center'>Visual Optimizer</div>
            </div>
            <div className={classNames('overflow-auto border-top border-bottom', className)} style={{gridColumn: 2, gridRow: 2}}>
                <div className='p-1'>
                    {alertProps !== undefined ? (
                        alertProps.children
                    ) : (
                        <>
                            <p>Please insert some Jova code or use the provided example code.</p>
                            <p>
                                Jova is an educational programming language which is similar to Java, but with several limitations. It is
                                object-oriented and can be compiled to Java bytecode.
                            </p>
                            The main differences between Java and Jova are:
                            <ul>
                                <li>
                                    <code>null</code> is called <code>nix</code> in Jova.
                                </li>
                                <li>
                                    <code>void</code> does not exist.
                                </li>
                                <li>Variable declarations are only allowed at the top of a method body.</li>
                                <li>
                                    <code>return</code> is only allowed at the bottom of a method body.
                                </li>
                                <li>
                                    <code>new Example</code> invokes the default constructor.
                                </li>
                                <li>
                                    <code>new Example(&hellip;)</code> invokes a declared constructor, as in Java.
                                </li>
                                <li>There are no packages and all classes are in one source file.</li>
                                <li>
                                    <code>static</code> does not exist, so the optional entry point of the program has to be a method called{' '}
                                    <code>main</code> that takes no arguments and returns an integer, inside a class called{' '}
                                    <code>Main</code> that contains only the main method.
                                </li>
                            </ul>
                            Jova has the following built-in functions:
                            <ul>
                                <li>
                                    <code>print(&hellip;)</code> either prints a boolean, an integer or a string on standard output.
                                </li>
                                <li>
                                    <code>readInt()</code> reads an integer from standard input.
                                </li>
                                <li>
                                    <code>readString()</code> reads a string from standard input.
                                </li>
                            </ul>
                        </>
                    )}
                </div>
            </div>
            <div className='p-3' style={{gridColumn: 2, gridRow: 3}}>
                <Button className='d-none mb-1' block onClick={() => reformat()} outline size='sm'>
                    Reformat Code
                </Button>
                <Button block type='submit' size='lg'>
                    {hasWarnings ? 'Start anyway' : 'Start'}
                </Button>
            </div>
        </Form>
    );
}

interface AlertProps {
    children: ReactNode;
    color: string;
}

function getPhaseAlertProps(
    phase: Phase | undefined,
    isWarning: boolean,
    messages: ReadonlyArray<PhaseMessage>,
    setLocation: (location: Location) => void
): AlertProps {
    const isTypeCheckerError = phase === 'TypeChecker' && !isWarning;
    return {
        children: (
            <>
                <b>
                    Number of {renderPhasePrefix(phase)} {isWarning ? 'warnings' : 'errors'}: {messages.length}
                </b>
                <ol className='m-0'>
                    {messages.map((message, index) => (
                        <li key={index}>{renderPhaseMessage(message, isTypeCheckerError, setLocation)}</li>
                    ))}
                </ol>
            </>
        ),
        color: isWarning ? 'warning' : 'danger',
    };
}
