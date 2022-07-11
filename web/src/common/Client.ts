import {AxiosStatic} from 'axios';
import {TypeCheckerResult} from 'model/TypeCheckerResult';
import {jovaString, typeCheckerResult} from 'examples/all01';
import {ClientReport} from 'model/ClientReport';

export class Client {
    constructor(private readonly axios: AxiosStatic, private readonly compilerUrl: string) {}

    postClientError(report: ClientReport<unknown>): Promise<void> {
        return this.axios.post(`${this.compilerUrl}/clientError`, report);
    }

    postClientWarning(report: ClientReport<unknown>): Promise<void> {
        return this.axios.post(`${this.compilerUrl}/clientWarning`, report);
    }

    async postTypeChecker(text: string): Promise<TypeCheckerResult> {
        if (text === jovaString) {
            return typeCheckerResult;
        }
        return (
            await this.axios.post<TypeCheckerResult>(`${this.compilerUrl}/typeChecker/jova`, text, {
                headers: {
                    'Content-Type': 'text/plain',
                },
            })
        ).data;
    }
}
