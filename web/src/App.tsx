import './App.scss';
import {useCallback, useState} from 'react';
import axios from 'axios';
import {Client} from 'common/Client';
import {AppView} from 'views/AppView';
import {compilerUrl} from 'common/Config';
import {Engine} from 'compiler';
import {Program} from 'compiler-generated';

export function App(): JSX.Element {
    const [client] = useState(() => new Client(axios, compilerUrl));
    const createStore = useCallback((program: Program) => Engine.createStore(program), []);
    return <AppView client={client} createStore={createStore} />;
}
