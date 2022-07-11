import {Button} from 'reactstrap';
import {MdiIcon} from './MdiIcon';
import classNames from 'classnames';
import './MdiButton.scss';

export function MdiButton({
    className,
    disabled,
    onClick,
    path,
    size,
    title,
}: {
    readonly className?: string;
    readonly disabled?: boolean;
    readonly onClick: () => void;
    readonly path: string;
    readonly size?: 'sm' | 'lg';
    readonly title?: string;
}): JSX.Element {
    return (
        <Button className={classNames('mdi-button', className)} disabled={disabled} onClick={onClick} size={size} title={title}>
            <MdiIcon path={path} title={title} width={'1.5rem'} />
        </Button>
    );
}
