import "vue-router";
import { Role } from "../gen-types";

declare module "vue-router" {
  interface RouteMeta {
    isAdmin?: boolean;
    requiresAuth?: boolean;
    disallowRoles?: Role[];
  }
}

export { };
