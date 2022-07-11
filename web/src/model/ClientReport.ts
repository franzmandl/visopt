import {ShortGitInfo} from 'common/Config';
import {HistoryState} from 'components/useHistory';
import {Parameter} from './Parameter';
import {TypeCheckerResult} from './TypeCheckerResult';

export interface ClientReport<T> {
    readonly client: string;
    readonly type: string;
    readonly componentName: string;
    readonly data: T;
    readonly location: string;
    readonly sessionUuid: string;
    readonly shortGitInfo: ShortGitInfo;
    readonly state: ClientState;
    readonly userAgent: string;
}

export type ClientState =
    | {readonly historyState: HistoryState; readonly parameter: Parameter}
    | {readonly text: string; readonly typeCheckerResult: TypeCheckerResult};
