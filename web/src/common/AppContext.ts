import {EngineResponse} from 'compiler';
import {ClientReport, ClientState} from 'model/ClientReport';
import {Dispatch} from 'react';
import {ModalContent} from '../components/ModalComponent';
import {Client} from './Client';
import {shortGitInfo} from './Config';
import {v4 as uuid4} from 'uuid';

export class AppContext {
    public readonly paramBodyIndex: number | null;
    private isFirstError = true;
    private isFirstWarning = true;
    private readonly sessionUuid = uuid4();
    private readonly userAgent = navigator.userAgent;

    constructor(
        public readonly client: Client,
        private readonly setLoading: Dispatch<boolean>,
        public readonly setModalContent: Dispatch<ModalContent | undefined>
    ) {
        const urlParams = new URLSearchParams(window.location.search);
        const paramBodyIndex = urlParams.get('bodyIndex');
        this.paramBodyIndex = paramBodyIndex !== null ? parseInt(paramBodyIndex) : null;
    }

    async load(block: () => Promise<void>): Promise<void> {
        this.setLoading(true);
        try {
            await block();
        } finally {
            this.setLoading(false);
        }
    }

    handleEngineError<T>(response: EngineResponse<T>, componentName: string, location: string, state: ClientState): T | undefined {
        const type = 'engine';
        switch (response.discriminator) {
            case 'EngineError':
                this.setModalContent({
                    header: 'An Optimizer Error occurred!',
                    body: 'This should never happen. The optimizer is now in an illegal state. If you proceed without reloading the page, some information might not be displayed correctly.',
                });
                this.reportClientError(this.createReport(type, componentName, response, location, state));
                return;
            case 'RangeError':
                this.setModalContent({
                    header: 'Maximum recursion depth exceeded!',
                    body: 'Simplify your program please.',
                });
                return;
            case 'Success': {
                const {warnings} = response;
                if (warnings !== undefined && warnings.length !== 0) {
                    this.reportClientWarning(this.createReport(type, componentName, response.warnings, location, state));
                }
                return response.payload;
            }
        }
    }

    reportRenderError(message: string, componentName: string, location: string, state: ClientState): void {
        this.reportClientError(this.createReport('render', componentName, message, location, state));
    }

    private reportClientError<T>(report: ClientReport<T>): void {
        console.error(report);
        if (this.isFirstError) {
            this.isFirstError = false;
            this.client.postClientError(report).catch((error) => console.error(error));
        }
    }

    private reportClientWarning<T>(report: ClientReport<T>): void {
        console.warn(report);
        if (this.isFirstWarning && this.isFirstError) {
            this.isFirstWarning = false;
            this.client.postClientWarning(report).catch((error) => console.error(error));
        }
    }

    private createReport<T>(type: string, componentName: string, data: T, location: string, state: ClientState): ClientReport<T> {
        return {
            client: 'web',
            type,
            componentName,
            data,
            location,
            sessionUuid: this.sessionUuid,
            state,
            shortGitInfo,
            userAgent: this.userAgent,
        };
    }
}
