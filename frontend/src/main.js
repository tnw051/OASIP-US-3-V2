import { createApp } from "vue";
import App from "./App.vue";
import "./index.css";
import router from "./router";
import { ApiErrorError } from "./service/common";

const app = createApp(App);

app.config.errorHandler = async (err, vm, info) => {
  if (err instanceof ApiErrorError) {
    if (err.content.status === 401) {
      await router.push("/login");
      return;
    }
  }
};

app.use(router).mount("#app");
