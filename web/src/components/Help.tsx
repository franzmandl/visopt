import './Help.scss';
import {MdiIcon} from 'components/mdi/MdiIcon';
import {mdiChevronDoubleLeft, mdiChevronDoubleRight, mdiChevronLeft, mdiChevronRight, mdiPlay} from '@mdi/js';
import {ReactNode} from 'react';

export function renderHelp(): ReactNode {
    return (
        <div className='help'>
            <div>
                <div>
                    <div>Use the navigation buttons bellow as follows:</div>
                    <div className='ps-3 pt-2'>Click {renderHelpButton(mdiChevronDoubleLeft)} to jump to the first step</div>
                    <div className='ps-3 pt-1'>Click {renderHelpButton(mdiChevronLeft)} to jump to the previous step</div>
                    <div className='ps-3 pt-1'>Click {renderHelpButton(mdiPlay)} to jump automatically</div>
                    <div className='ps-3 pt-1'>Click {renderHelpButton(mdiChevronRight)} to jump to the next step</div>
                    <div className='ps-3 pt-1'>Click {renderHelpButton(mdiChevronDoubleRight)} to jump to the last step</div>
                </div>
            </div>
        </div>
    );
}

function renderHelpButton(path: string): ReactNode {
    return (
        <span className='btn btn-secondary help-button'>
            <MdiIcon path={path} />
        </span>
    );
}
