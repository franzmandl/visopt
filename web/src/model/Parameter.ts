import {BodyAddress, LoopMode, Program, Variable} from 'compiler-generated';
import {Optimization} from './Optimization';

export interface Parameter {
    readonly address: BodyAddress;
    readonly commandPosition: number;
    readonly optimizations: ReadonlyArray<Optimization>;
    readonly mode: LoopMode;
    readonly liveOnExit: ReadonlyArray<Variable>;
    readonly program: Program;
    readonly threeAddressCode: boolean;
}
