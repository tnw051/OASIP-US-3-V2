import { createRouter, createWebHistory } from "vue-router";
import { useAuth } from "../utils/useAuth";
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

const { isAuthenticated, user } = useAuth();

// guard /users route
router.beforeEach((to, from) => {
  const isAuth = isAuthenticated.value;
  if (to.name === "users" && (!isAuth || user.value?.role?.toLowerCase() !== "admin")) {
    return { name: "login" };
  }
});

export default router;