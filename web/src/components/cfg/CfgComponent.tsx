import {Cfg} from 'compiler-generated';
import {ReactNode, useMemo} from 'react';
import {CfgNodeComponent, IndexMapping} from './CfgNodeComponent';
import './CfgComponent.scss';
import classNames from 'classnames';

export function CfgComponent({
    cfg,
    className,
    onRenderError,
    renderContent,
}: {
    readonly cfg: Cfg;
    readonly className?: string;
    readonly onRenderError: (message: string, componentName: string, location: string) => void;
    readonly renderContent: (id: number) => ReactNode;
}): JSX.Element {
    const indexMapping = useMemo(() => createIndexMapping(cfg), [cfg]);
    return (
        <div className={classNames('cfg-component', className)}>
            {cfg.list.map((cfgNode) => (
                <CfgNodeComponent
                    cfgNode={cfgNode}
                    indexMapping={indexMapping}
                    key={cfgNode.id}
                    onRenderError={onRenderError}
                    renderContent={renderContent}
                />
            ))}
        </div>
    );
}

function createIndexMapping(cfg: Cfg): IndexMapping {
    const indexMapping: IndexMapping = {};
    cfg.list.forEach((cfgNode, index) => {
        indexMapping[cfgNode.id] = index;
    });
    return indexMapping;
}
