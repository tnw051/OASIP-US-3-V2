import { createRouter, createWebHistory } from "vue-router";
import { roles, useAuth } from "../utils/useAuth";
import CategoryEvent from "../views/CategoryEvent.vue";
import CreateEvent from "../views/CreateEvent.vue";
import CreateUser from "../views/CreateUser.vue";
import Events from "../views/Events.vue";
import Login from "../views/Login.vue";
import NotFound from "../views/NotFound.vue";
import Users from "../views/Users.vue";

const history = createWebHistory(import.meta.env.BASE_URL);

/** @type {import('vue-router').RouteRecordRaw[]} */
const routes = [
  {
    path: "/",
    name: "home",
    component: Events,
    alias: ["/events", "/home"],
  },
  {
    path: "/create-event",
    name: "createEvent",
    component: CreateEvent,
    meta: {
      disallowRoles: [roles.LECTURER],
    },
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
      isAdmin: true,
    },
  },
  {
    path: "/create-user",
    name: "createUser",
    component: CreateUser,
    meta: {
      requiresAuth: true,
      isAdmin: true,
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

const { isAuthenticated, isAdmin, isAuthLoading, user } = useAuth();

router.beforeEach(async (to, from) => {
  // wait for auth to initialize before each route
  while (isAuthLoading.value) {
    await new Promise((resolve) => setTimeout(resolve, 100));
  }

  if ((to.meta.requiresAuth && !isAuthenticated.value)) {
    console.log(`redirecting to login (will redirect to ${to.path} after login)`);
    return { name: "login", query: { redirect: to.fullPath } };
  }

  if (to.meta.isAdmin && !isAdmin.value) {
    console.log("redirecting to home (not admin)");
    return { name: "home" };
  }

  if (to.meta.disallowRoles && to.meta.disallowRoles.includes(user.value?.role)) {
    console.log(`redirecting to home (role ${user.value?.role} not allowed)`);
    return { name: "home" };
  }
});

export default router;