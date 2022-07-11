import {Command} from 'compiler-generated';

export type Step =
    | ({readonly discriminator: 'FirstStep'} & FirstStep)
    | ({readonly discriminator: 'BeforeStep'} & BeforeStep)
    | ({readonly discriminator: 'AfterStep'} & AfterStep)
    | ({readonly discriminator: 'LastStep'} & LastStep);

export interface FirstStep {
    position: number;
}

export interface BeforeStep {
    command: Command;
    position: number;
}

export interface AfterStep {
    command: Command;
    position: number;
}

export interface LastStep {
    position: number;
}
