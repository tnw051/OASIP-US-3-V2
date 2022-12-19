import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import { useAuthStore } from "../auth/useAuthStore";
import CategoryEvent from "../views/CategoryEvent.vue";
import CreateUser from "../views/CreateUser.vue";
import Events from "../views/Events.vue";
import Login from "../views/Login.vue";
import NotFound from "../views/NotFound.vue";
import Users from "../views/Users.vue";

const history = createWebHistory(import.meta.env.BASE_URL);

const routes: RouteRecordRaw[] = [
  {
    path: "/",
    name: "home",
    component: Events,
    alias: ["/events", "/home"],
  },
  {
    path: "/categories",
    name: "categories",
    component: CategoryEvent,
  },
  {
    path: "/users",
    name: "users",
    component: Users,
    meta: {
      requiresAuth: true,
      checkAuth(auth) {
        return auth.isAdmin;
      },
    },
  },
  {
    path: "/create-user",
    name: "createUser",
    component: CreateUser,
    meta: {
      requiresAuth: true,
      checkAuth(auth) {
        return auth.isAdmin;
      },
    },
  },
  {
    path: "/login",
    name: "login",
    component: Login,
  },
  {
    path: "/:catchAll(.*)",
    name: "notFound",
    component: NotFound,
  },
];

const router = createRouter({
  history,
  routes,
});

const { isAuthenticated, user, state } = useAuthStore();

router.beforeEach((to, from) => {
  const { requiresAuth, checkAuth } = to.meta;

  if (to.name === "login" && isAuthenticated.value) {
    return { name: "home" };
  }

  if (requiresAuth && !isAuthenticated.value) {
    console.log(`redirecting to login (will redirect to ${to.path} after login)`);
    return { name: "login", query: { redirect: to.fullPath } };
  }

  const role = user.value?.role;
  if (checkAuth && !checkAuth(state.value)) {
    console.log(`redirecting to home (role ${role} not allowed)`);
    return { name: "home" };
  }
});

export default router;