import Enzyme from 'enzyme';
import Adapter from '@wojtekmaj/enzyme-adapter-react-17';

Enzyme.configure({adapter: new Adapter()});

jest.setTimeout(50000);

(global as any).screenWidth = 1920;

afterEach(async () => {
    await new Promise(setImmediate);
});

const warn = console.warn;
console.warn = (...args: unknown[]) => {
    warn.apply(console, args);
    throw new Error('' + args);
};

const error = console.error;
console.error = (...args: unknown[]) => {
    error.apply(console, args);
    throw new Error('' + args);
};
