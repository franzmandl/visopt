declare module 'compiler' {
    import {Address, BodyAddress, Command, LoopMode, Program, Variable} from 'compiler-generated';

    export type EngineResponse<T> =
        | ({readonly discriminator: 'Success'} & SuccessResponse<T>)
        | {readonly discriminator: 'EngineError' | 'RangeError'};

    export interface SuccessResponse<T> {
        readonly payload: T;
        readonly warnings?: ReadonlyArray<string>;
    }

    export interface CreateStoreResult {
        readonly bodyAddresses: ReadonlyArray<BodyAddress>;
        readonly initialProgram: Program;
        readonly store: Store;
    }

    export type CreateStore = (program: Program) => EngineResponse<CreateStoreResult>;

    export namespace Engine {
        export const infiniteLoopMode: LoopMode;
        export const onceLoopMode: LoopMode;
        export const createStore: CreateStore;
        export const triggerError: () => EngineResponse<void>;
    }

    export interface JasminCode {
        readonly before: string;
        readonly active: string;
        readonly after: string;
    }

    export interface StartCommandCursorResult {
        readonly commandCursor: CommandCursor;
        readonly commands: ReadonlyArray<Command>;
    }

    export interface Store {
        readonly getJasminCode: (address: BodyAddress, activeAddress: Address | null) => EngineResponse<JasminCode>;
        readonly getJovaCode: (address: Address) => EngineResponse<string>;
        readonly getVariables: (address: Address) => EngineResponse<ReadonylArray<Variable>>;
        readonly startCommandCursor: (
            address: Address,
            optimizations: ReadonlyArray<string>,
            mode: LoopMode,
            liveOnExit: ReadonlyArray<Variable>
        ) => EngineResponse<StartCommandCursorResult>;
        readonly setProgram: (program: Program) => EngineResponse<void>;
    }

    export interface MoveResult {
        readonly position: number;
        readonly program: Program;
    }

    export interface CommandCursor {
        readonly move: (currentPosition: number, updatedPosition: number, program: Program) => EngineResponse<MoveResult>;
    }
}
