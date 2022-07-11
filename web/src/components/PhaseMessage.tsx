import {Location, Phase, PhaseMessage} from 'compiler-generated';
import {ReactNode} from 'react';

export function renderPhasePrefix(phase: string | undefined): ReactNode {
    switch (phase as Phase) {
        case 'Lexer':
            return 'lexical';
        case 'Parser':
            return 'syntax';
        case 'TypeChecker':
            return 'type';
        default:
            return 'unknown';
    }
}

export function renderPhaseMessage(
    {location, text}: PhaseMessage,
    isTypeCheckerError: boolean,
    setLocation: (location: Location) => void
): ReactNode {
    return isTypeCheckerError ? (
        <>
            {text} (
            <a href='#' onClick={() => setLocation(location)}>
                line {location.line}
            </a>
            )
        </>
    ) : (
        <>
            <a href='#' onClick={() => setLocation(location)}>
                line {renderLocation(location)}
            </a>{' '}
            {text}
        </>
    );
}

function renderLocation({line, position}: Location): string {
    return `${line}:${position + 1}`;
}
