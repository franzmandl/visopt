import {Engine} from 'compiler';
import {BodyAddress, Program, Variable} from 'compiler-generated';
import {Parameter} from 'model/Parameter';
import {allOptimizations, Optimization} from 'model/Optimization';
import {Dispatch, useCallback, useState} from 'react';

export function useParameter(
    address: BodyAddress,
    program: Program
): {
    readonly parameter: Parameter;
    readonly setAddress: Dispatch<BodyAddress>;
    readonly setCommandPosition: Dispatch<number>;
    readonly setLiveOnExit: Dispatch<ReadonlyArray<Variable>>;
    readonly setOptimizations: Dispatch<ReadonlyArray<Optimization>>;
    readonly setParameter: Dispatch<Parameter>;
    readonly setThreeAddressCode: Dispatch<boolean>;
} {
    const [parameter, setParameter] = useState<Parameter>({
        address,
        commandPosition: 0,
        liveOnExit: [],
        mode: Engine.infiniteLoopMode,
        optimizations: allOptimizations,
        program,
        threeAddressCode: false,
    });

    const setAddress = useCallback(
        (address: BodyAddress) => setParameter((prevParameter) => ({...prevParameter, liveOnExit: [], address})),
        []
    );

    const setCommandPosition = useCallback(
        (commandPosition: number) => setParameter((prevParameter) => ({...prevParameter, commandPosition})),
        []
    );

    const setLiveOnExit = useCallback(
        (liveOnExit: ReadonlyArray<Variable>) => setParameter((prevParameter) => ({...prevParameter, liveOnExit})),
        []
    );

    const setOptimizations = useCallback(
        (optimizations: ReadonlyArray<Optimization>) => setParameter((prevParameter) => ({...prevParameter, optimizations})),
        []
    );

    const setThreeAddressCode = useCallback(
        (threeAddressCode: boolean) => setParameter((prevParameter) => ({...prevParameter, threeAddressCode})),
        []
    );

    return {
        parameter,
        setAddress,
        setCommandPosition,
        setLiveOnExit,
        setOptimizations,
        setParameter,
        setThreeAddressCode,
    };
}
