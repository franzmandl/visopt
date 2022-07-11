import {CreateStoreResult, Engine, StartCommandCursorResult} from 'compiler';
import {Variable} from 'compiler-generated';
import {AppContext} from 'common/AppContext';
import {Dispatch, ReactNode, useCallback, useEffect, useState} from 'react';
import {Button, ButtonGroup, Form, FormGroup, Label} from 'reactstrap';
import {OptimizationSelect} from 'components/OptimizationSelect';
import {BodyAddressSelect} from 'components/BodyAddressSelect';
import {ControlsComponent} from 'components/ControlsComponent';
import './ProgramView.scss';
import {HeaderComponent} from 'components/HeaderComponent';
import {ReasonComponent} from 'components/ReasonComponent';
import {VariableSelect} from 'components/VariableSelect';
import {findSymbol} from 'components/ast/AstUtil';
import {HistoryState, useHistory} from 'components/useHistory';
import {useParameter} from 'components/useParameter';
import {FormGroupCheckbox} from 'components/FormGroupCheckbox';
import {Nav, NavItem, NavLink} from 'reactstrap';
import {Parameter} from 'model/Parameter';
import {renderFollowCheckbox, useAutoCenterElement} from 'components/useAutoCenterElement';
import {useLatest} from 'components/useLatest';
import {useStepCursor} from 'components/useStepCursor';
import {renderHelp} from 'components/Help';
import {renderCode, CodeId} from 'components/CodeComponent';
import {Optimization} from 'model/Optimization';

const componentName = 'ProgramView';
export function ProgramView({
    context,
    clearStore: parentClearStore,
    createStoreResult: {bodyAddresses, initialProgram, store},
}: {
    readonly context: AppContext;
    readonly clearStore: (nextDefaultText: string | undefined) => void;
    readonly createStoreResult: CreateStoreResult;
}): JSX.Element {
    const [selectedCodeId, setSelectedCodeId] = useState(CodeId.FlowGraph);
    const [rawSpeed, setRawSpeed] = useState(0);
    const [allVariables, setAllVariables] = useState<ReadonlyArray<Variable>>([]);
    const [stepThrough, setStepThrough] = useState(true);
    const [stepPosition, setStepPosition] = useState(0);
    const [startCommandCursorResult, setStartCommandCursorResult] = useState<StartCommandCursorResult>();
    const stepCursor = useStepCursor(startCommandCursorResult);
    const step = stepCursor?.steps[stepPosition];
    const autoCenterElement = useAutoCenterElement(
        startCommandCursorResult !== undefined && stepPosition !== 0 && stepPosition !== stepCursor?.maximumPosition
    );

    const {parameter, setAddress, setCommandPosition, setLiveOnExit, setOptimizations, setParameter, setThreeAddressCode} = useParameter(
        bodyAddresses[context.paramBodyIndex ?? 0],
        initialProgram
    );

    const [workingProgram, setWorkingProgram] = useState(parameter.program);

    const setPresentParameter = useCallback(
        (nextParameter: Parameter, historyState: HistoryState) => {
            context.handleEngineError(store.setProgram(nextParameter.program), componentName, 'Method setPresentParameter', {
                parameter: parameter,
                historyState,
            });
            setWorkingProgram(nextParameter.program);
            setParameter(nextParameter);
        },
        [parameter, context, setParameter, store]
    );

    const history = useHistory(parameter, setPresentParameter);
    const symbol = findSymbol(workingProgram, parameter.address);
    const clientStateRef = useLatest({historyState: history.state, parameter});

    const clearStore = useCallback(() => {
        parentClearStore(
            context.handleEngineError(
                store.getJovaCode({discriminator: 'ProgramAddress'}),
                componentName,
                'Method clearCursor',
                clientStateRef.current
            )
        );
    }, [parentClearStore, context, clientStateRef, store]);

    const clearCursor = useCallback(() => {
        if (parameter.commandPosition != 0) {
            history.append({...parameter, program: workingProgram});
        }
        setStartCommandCursorResult(undefined);
    }, [history, parameter, workingProgram]);

    const startCursor = useCallback(() => {
        setCommandPosition(0);
        setStepPosition(0);
        const result = context.handleEngineError(
            store.startCommandCursor(
                {discriminator: 'BodyAddress', ...parameter.address},
                parameter.threeAddressCode ? [...parameter.optimizations, Optimization.ThreeAddressCode] : parameter.optimizations,
                parameter.mode,
                parameter.liveOnExit
            ),
            componentName,
            'Method startCursor',
            clientStateRef.current
        );
        if (result === undefined) {
            return;
        }
        if (result.commands.length === 0) {
            context.setModalContent({
                header: 'Program is already optimized!',
                body: 'The selected optimizations would not change the program any more.',
            });
            return;
        }
        return result;
    }, [
        setCommandPosition,
        context,
        store,
        parameter.address,
        parameter.threeAddressCode,
        parameter.optimizations,
        parameter.mode,
        parameter.liveOnExit,
        clientStateRef,
    ]);

    const skipCursor = useCallback(() => {
        const result1 = startCursor();
        if (result1 === undefined) {
            return;
        }
        const {commandCursor, commands} = result1;
        const result2 = context.handleEngineError(
            commandCursor.move(0, commands.length, workingProgram),
            componentName,
            'Method skipCursor',
            clientStateRef.current
        );
        if (result2 === undefined) {
            return;
        }
        const {position: commandPosition, program} = result2;
        setCommandPosition(commandPosition);
        setWorkingProgram(program);
        if (commandPosition != 0) {
            history.append({...parameter, commandPosition, program});
        }
    }, [startCursor, context, workingProgram, clientStateRef, setCommandPosition, history, parameter]);

    const doOptimization = useCallback(() => {
        context.load(() => {
            return new Promise((resolve) => {
                setTimeout(() => {
                    if (stepThrough) {
                        setStartCommandCursorResult(startCursor());
                    } else {
                        skipCursor();
                    }
                    resolve();
                });
            });
        });
    }, [context, skipCursor, startCursor, stepThrough]);

    const loadAllVariables = useCallback(() => {
        const result = context.handleEngineError(
            store.getVariables({discriminator: 'BodyAddress', ...parameter.address}),
            componentName,
            'Method loadAllVariables',
            clientStateRef.current
        );
        setAllVariables(result ?? []);
    }, [parameter.address, context, clientStateRef, store]);

    useEffect(loadAllVariables, [parameter.program, loadAllVariables]);

    const logClientState = useCallback(() => {
        console.log('logClientState', clientStateRef.current);
    }, [clientStateRef]);

    const triggerEngineError = useCallback(() => {
        context.handleEngineError(Engine.triggerError(), componentName, 'Method triggerEngineError', clientStateRef.current);
    }, [context, clientStateRef]);

    const onRenderError = useCallback(
        (message: string, componentName: string, location: string) => {
            context.reportRenderError(message, componentName, location, clientStateRef.current);
        },
        [context, clientStateRef]
    );

    return (
        <div className='program-view d-grid h-100'>
            <div className='p-1' style={{display: 'none', gridColumn: '1 / 3', gridRow: 4, zIndex: 2000}}>
                <ButtonGroup>
                    <Button onClick={logClientState}>logClientState</Button>
                    <Button onClick={triggerEngineError}>triggerEngineError</Button>
                </ButtonGroup>
            </div>
            <div
                className='border-end h-100 overflow-auto p-4 position-relative'
                onScroll={autoCenterElement.onParentScroll}
                style={{gridColumn: 1, gridRow: '1 / 4'}}
            >
                <div className='position-absolute top-0 start-0' ref={autoCenterElement.zeroElementRef} />
                {symbol !== undefined &&
                    renderCode(
                        selectedCodeId,
                        parameter.address,
                        autoCenterElement.centerElement,
                        context,
                        clientStateRef,
                        onRenderError,
                        step,
                        store,
                        symbol
                    )}
            </div>
            <div className='p-3' style={{gridColumn: 2, gridRow: 1}}>
                <HeaderComponent
                    clearCursor={clearCursor}
                    clearStore={clearStore}
                    context={context}
                    hasCursor={stepCursor !== undefined}
                    history={history}
                />
                <hr />
                {renderNavItems(selectedCodeId, setSelectedCodeId)}
                <hr className='mb-0' />
            </div>
            <div className='h-100 overflow-auto px-3' style={{gridColumn: 2, gridRow: 2}}>
                {step === undefined ? (
                    <Form>
                        <FormGroup>
                            <Label for='bodyAddressSelect'>
                                <b>Method</b>
                            </Label>
                            <BodyAddressSelect
                                bodyAddresses={bodyAddresses}
                                htmlId='bodyAddressSelect'
                                selected={parameter.address}
                                setSelected={setAddress}
                            />
                        </FormGroup>
                        <div className='mb-2'>
                            <b>Optimizations</b>
                            <OptimizationSelect selected={parameter.optimizations} setSelected={setOptimizations} />
                        </div>
                        <div className='mb-2'>
                            <b>Three-Address Code</b>
                            <FormGroupCheckbox
                                checked={parameter.threeAddressCode}
                                htmlId='threeAddressCode'
                                setChecked={setThreeAddressCode}
                            >
                                Transform to Three-Address Code first
                            </FormGroupCheckbox>
                        </div>
                        <div className='mb-2'>
                            <b>Live on Exit</b>
                            <VariableSelect allVariables={allVariables} selected={parameter.liveOnExit} setSelected={setLiveOnExit} />
                        </div>
                    </Form>
                ) : step.discriminator === 'BeforeStep' || step.discriminator === 'AfterStep' ? (
                    <ReasonComponent before={step.discriminator === 'BeforeStep'} command={step.command} />
                ) : (
                    renderHelp()
                )}
            </div>
            <div className='p-3' style={{gridColumn: 2, gridRow: 3}}>
                {stepCursor === undefined ? (
                    <>
                        <hr className='mt-0' />
                        <div className='mb-3'>
                            <b>Visualization</b>
                            <FormGroupCheckbox checked={stepThrough} htmlId='stepThrough' setChecked={setStepThrough}>
                                Show each transformation step
                            </FormGroupCheckbox>
                            {renderFollowCheckbox(autoCenterElement, !stepThrough)}
                        </div>
                        <Button block onClick={doOptimization} size='lg'>
                            {stepThrough ? 'Start Optimizer' : 'Optimize'}
                        </Button>
                    </>
                ) : (
                    <ControlsComponent
                        context={context}
                        historyState={history.state}
                        parameter={parameter}
                        program={workingProgram}
                        rawSpeed={rawSpeed}
                        stepCursor={stepCursor}
                        stepPosition={stepPosition}
                        setCommandPosition={setCommandPosition}
                        setProgram={setWorkingProgram}
                        setRawSpeed={setRawSpeed}
                        setStepPosition={setStepPosition}
                    >
                        {renderFollowCheckbox(autoCenterElement, false)}
                    </ControlsComponent>
                )}
            </div>
        </div>
    );
}

function renderNavItems(selectedCodeId: CodeId, setSelectedCodeId: Dispatch<CodeId>): ReactNode {
    return (
        <Nav className='justify-content-center' pills>
            {renderNavItem(CodeId.FlowGraph, 'Flow Graph', selectedCodeId, setSelectedCodeId)}
            {renderNavItem(CodeId.Jova, 'Jova', selectedCodeId, setSelectedCodeId)}
            {renderNavItem(CodeId.Jasmin, 'Jasmin', selectedCodeId, setSelectedCodeId)}
        </Nav>
    );
}

function renderNavItem(codeId: CodeId, label: string, selectedCodeId: CodeId, setSelectedCodeId: Dispatch<CodeId>): JSX.Element {
    return (
        <NavItem>
            <NavLink active={selectedCodeId === codeId} href='#' onClick={() => setSelectedCodeId(codeId)}>
                {label}
            </NavLink>
        </NavItem>
    );
}
