import { createApp } from "vue";
import { createPinia } from "pinia";
import {
  loadRuntimeConfiguration,
  runtimeConfiguration,
} from "./config/runtimeConfiguration";
import { OpenAPI } from "@/data/api/client";

import "./assets/main.scss";

import App from "./App.vue";
import router from "./router";

const initApp = async () => {
  const app = createApp(App);
  const runtimeConfigurationOptions = await loadRuntimeConfiguration();
  OpenAPI.BASE = runtimeConfigurationOptions.variables.apiBaseUrl;

  app.use(runtimeConfiguration, runtimeConfigurationOptions);
  app.use(createPinia());
  app.use(router);

  app.mount("#app");
};
initApp();
