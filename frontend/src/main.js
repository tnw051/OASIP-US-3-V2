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

  // refresh the page when the error is unhandled
  router.go(0);
};

app.use(router).mount("#app");
