import "vue-router";

declare module "vue-router" {
  interface RouteMeta {
    isAdmin?: boolean;
    requiresAuth?: boolean;
    disallowRoles?: string[];
  }
}

export { };
