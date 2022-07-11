import {Spinner} from 'reactstrap';
import './LoadingIndicator.scss';

export function LoadingIndicator(): JSX.Element {
    return (
        <div className='loading-indicator' tabIndex={-1}>
            <div>
                <Spinner className='loading-indicator-spinner' color='light' tabIndex={-1}>
                    {''}
                </Spinner>
                <div className='modal-backdrop show'></div>
            </div>
        </div>
    );
}
