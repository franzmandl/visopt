export interface SpringError {
    readonly timestamp: number;
    readonly status: number;
    readonly error: string;
    readonly path: string;
}

export function instanceOfSpringError(o: unknown): o is SpringError {
    const t = o as Partial<SpringError>;
    return typeof t?.timestamp === 'number' && typeof t?.status === 'number' && typeof t?.error === 'string' && typeof t?.path === 'string';
}
