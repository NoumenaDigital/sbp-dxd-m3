import {
  describe,
  expect,
  it,
} from 'vitest';
import { defineComponent } from 'vue';
import { mount } from '@vue/test-utils';

import {
  type RuntimeConfiguration,
  type RuntimeConfigurationOptions,
  runtimeConfiguration,
} from '../runtimeConfiguration';

const App = defineComponent({
  data() {
    const runtimeConfigurationResult = (this.$runtimeConfiguration as unknown as RuntimeConfiguration);

    const {
      apiBaseUrl,
    } = runtimeConfigurationResult;

    const dataString = apiBaseUrl;

    return {
      dataString,
    };
  },
  template: '<template>{{dataString}}</template>',
});

describe('runtimeConfiguration plugin', () => {
  it('resolves configuration parameters from injected configuration options', () => {
    const wrapper = mount(App, {
      global: {
        plugins: [[runtimeConfiguration, {
          variables: {
            apiBaseUrl: '001',
          },
        } as RuntimeConfigurationOptions]],
      },
    });
    expect(wrapper.text()).toBe('001');
  });
});
