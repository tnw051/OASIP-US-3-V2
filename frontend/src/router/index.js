import { createRouter, createWebHistory } from "vue-router";
import CategoryEvent from "../views/CategoryEvent.vue";
import CreateEvent from "../views/CreateEvent.vue";
import CreateUser from "../views/CreateUser.vue";
import Events from "../views/Events.vue";
import Login from '../views/Login.vue';
import Users from "../views/Users.vue";

const history = createWebHistory(import.meta.env.BASE_URL);

const routes = [
  {
    path: "/",
    name: "home",
    component: Events,
  },
  {
    path: "/create-event",
    name: "createEvent",
    component: CreateEvent,
  },
  {
    path: "/categories",
    name: "categories",
    component: CategoryEvent,
  },
  {
    path: "/users",
    name: "users",
    component: Users
  },
  {
    path: "/create-user",
    name: "createUser",
    component: CreateUser,
  },
  {
    path: "/login",
    name: "login",
    component: Login,
  }
];

const router = createRouter({
  history,
  routes,
});

export default router;
