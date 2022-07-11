import Icon from '@mdi/react';

export function MdiIcon({
    path,
    title,
    width = '1em',
}: {
    readonly path: string;
    readonly title?: string;
    readonly width?: string;
}): JSX.Element {
    return <Icon path={path} style={{width}} title={title} />;
}
