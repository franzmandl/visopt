import {getCompilerUrl} from './Config';

describe('Config', () => {
    it('getCompilerUrl', () => {
        expect(getCompilerUrl('http://localhost:3000')).toEqual('http://localhost:3000/compiler');
        expect(getCompilerUrl('http://localhost:3000/')).toEqual('http://localhost:3000/compiler');
        expect(getCompilerUrl('http://localhost:3000/index.html')).toEqual('http://localhost:3000/compiler');
        expect(getCompilerUrl('http://localhost:3000/context')).toEqual('http://localhost:3000/context/compiler');
        expect(getCompilerUrl('http://localhost:3000/context/')).toEqual('http://localhost:3000/context/compiler');
        expect(getCompilerUrl('http://localhost:3000/context/index.html')).toEqual('http://localhost:3000/context/compiler');
    });
});
