import fs from 'fs';
import {CreateStoreResult, Engine, EngineResponse} from 'compiler';
import {allOptimizations} from 'model/Optimization';

describe('Engine', () => {
    const testCasesDirectories = [
        '../compiler/src/jvmTest/resources/cases/code',
        ...(process.env['TEST_CASES_PATH'] ?? '').split(',').filter((path) => path.length > 0),
    ];
    const inJsonName = 'in.json';
    const refWebJsonName = 'ref/web.json';

    function handleError<T>(response: EngineResponse<T>): T {
        if (response.discriminator !== 'Success') {
            throw Error(JSON.stringify(response, null, 2));
        }
        return response.payload;
    }

    function getRef(directory: string): Ref {
        const path = directory + '/' + refWebJsonName;
        if (fs.existsSync(path)) {
            return JSON.parse(fs.readFileSync(path).toString());
        } else {
            return {};
        }
    }

    function visitDirectory(directory: string) {
        const childNames = fs.readdirSync(directory);
        if (childNames.indexOf(inJsonName) !== -1) {
            console.log(directory);
            const ref = getRef(directory);
            const jsonString = fs.readFileSync(directory + '/' + inJsonName).toString();
            let program = JSON.parse(jsonString).program;
            const createStoreResponse = Engine.createStore(program);
            if (ref.createStore !== undefined) {
                if (ref.createStore.discriminator === 'RangeError' && ref.createStore.discriminator === createStoreResponse.discriminator) {
                    return;
                }
            }
            const {store} = handleError(createStoreResponse);
            const {commandCursor, commands} = handleError(
                store.startCommandCursor({discriminator: 'ProgramAddress'}, allOptimizations, Engine.infiniteLoopMode, [])
            );
            let commandPosition = 0;
            while (commandPosition < commands.length) {
                const moveResult = handleError(commandCursor.move(commandPosition, commandPosition + 1, program));
                commandPosition = moveResult.position;
                program = moveResult.program;
            }
        } else {
            childNames.forEach((childName) => {
                const childPath = directory + '/' + childName;
                if (fs.lstatSync(childPath).isDirectory()) {
                    visitDirectory(childPath);
                }
            });
        }
    }

    it('all', () => {
        testCasesDirectories.forEach((testCasesDirectory) => visitDirectory(testCasesDirectory));
    });
});

interface Ref {
    readonly createStore?: EngineResponse<CreateStoreResult>;
}
