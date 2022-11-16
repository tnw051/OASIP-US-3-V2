import "vue-router";
import { AuthState } from "../auth/useAuthStore";

declare module "vue-router" {
  interface RouteMeta {
    isAdmin?: boolean;
    requiresAuth?: boolean;
    checkAuth?: (auth: AuthState) => boolean;
  }
}

export { };
