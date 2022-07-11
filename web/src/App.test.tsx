import {mount, ReactWrapper} from 'enzyme';
import {App} from './App';

describe('<App/>', () => {
    let component: ReactWrapper;

    beforeEach(async () => {
        component = mount(<App />);
    });

    it('test 1', async () => {
        expect(component).toBeDefined();
    });
});
