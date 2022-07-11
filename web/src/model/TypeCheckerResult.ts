import {Phase, PhaseMessage, Program} from 'compiler-generated';

export interface TypeCheckerResult {
    readonly program: Program;
    readonly typeWarnings: ReadonlyArray<PhaseMessage>;
}

export interface TypeCheckerError {
    readonly discriminator: 'PhaseMessages';
    readonly phase: Phase;
    readonly isWarning: boolean;
    readonly messages: ReadonlyArray<PhaseMessage>;
}

export function instanceOfTypeCheckerError(o: unknown): o is TypeCheckerError {
    const t = (o ?? {}) as Partial<TypeCheckerError>;
    return (
        t.discriminator === 'PhaseMessages' && typeof t.phase === 'string' && typeof t.isWarning === 'boolean' && Array.isArray(t.messages)
    );
}
