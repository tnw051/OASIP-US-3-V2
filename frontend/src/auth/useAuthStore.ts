import {
  computed,
  Ref,
  ref,
  watch,
  watchEffect,
} from "vue";

type Profile = {
  id: string;
  name: string;
  email: string;
  role: string;
  roles?: string[];
};

export type AuthState = {
  user: Profile | null;
  status: "authenticated" | "loading" | "unauthenticated";
  isAdmin: boolean;
  isLecturer: boolean;
  isStudent: boolean;
  isGuest: boolean;
}

type Status = {
  isLoading: boolean;
  isAuthenticated: boolean;
  isUnauthenticated: boolean;
}

type AuthStateExtended = AuthState & Status

export type AuthStore = {
  id: string;
  name: string;
  logout: () => Promise<boolean>;
  preload: () => Promise<void>;
  state: Ref<AuthState>;
  getAccessToken: () => Promise<string | null>;
  onRefreshTokenFailed?: () => Promise<void>;
};


const logger = (() => {
  const context = "[useAuthStore]";
  const bakedLog = function () {
    return console.log.bind(console, context);
  }();
  const bakedError = function () {
    return console.error.bind(console, context);
  }();

  return {
    log: bakedLog,
    error: bakedError,
  };
})();

export function getDefaultAuthState(): AuthStateExtended {
  return {
    isAdmin: false,
    isLecturer: false,
    isStudent: false,
    isGuest: true,
    user: null,
    status: "unauthenticated",
    isLoading: false,
    isAuthenticated: false,
    isUnauthenticated: true,
  };
}

const state = ref<AuthStateExtended>(getDefaultAuthState());
const authStore = ref<AuthStore | null>();
const authStores: Map<string, AuthStore> = new Map();
const isPreloadDone = ref(false);

const authStoreIdKey = "authStoreId";

let stopAuthStoreStateWatch: () => void;

export function registerAuthStore(store: AuthStore) {
  authStores.set(store.id, store);
  logger.log(`registered store ${store.id} (${store.name})`);
}

async function preload() {
  const storeId = localStorage.getItem(authStoreIdKey);
  try {
    if (storeId) {
      const store = authStores.get(storeId);
      if (store) {
        logger.log(`preload: from ${store.id}`);
        setStore(store);
        await store.preload();
        console.log("preload: done");
      } else {
        logger.error(`preload: ${storeId} not registered, aborting`);
      }
    }
  } catch (e) {
    logger.error("preload: error", e);
  } finally {
    isPreloadDone.value = true;
  }
}

export function setStore(storeConfig: AuthStore) {
  stopAuthStoreStateWatch?.();
  authStore.value = storeConfig;

  logger.log(`setStore: ${storeConfig.id} - watching for state changes`);
  stopAuthStoreStateWatch = watch(storeConfig.state, (newState) => {
    logger.log(`${storeConfig.id} changed state`, newState);
    state.value = {
      ...newState,
      ...getStatusFromState(newState),
    };

  }, { deep: true });

  localStorage.setItem(authStoreIdKey, storeConfig.id);
  logger.log(`setStore: set ${authStoreIdKey} to ${storeConfig.id} in localStorage`);
}

function getStatusFromState(state: AuthState): Status {
  return {
    isLoading: state.status === "loading",
    isAuthenticated: state.status === "authenticated",
    isUnauthenticated: state.status === "unauthenticated",
  };
}

async function logout(): Promise<boolean> {
  if (!authStore.value) {
    return false;
  }
  localStorage.removeItem(authStoreIdKey);

  logger.log(`logout: from ${authStore.value.name}, clearing state`);
  const logoutFunc = authStore.value.logout;
  state.value = getDefaultAuthState();
  authStore.value = null;
  
  return await logoutFunc();
}

interface UseAuthStore {
  state: Ref<AuthStateExtended>;
  logout: () => Promise<boolean>;
  setStore: (store: AuthStore) => void;
  // shorthand for state value
  isAuthenticated: Ref<boolean>;
  isAuthLoading: Ref<boolean>;
  isUnauthenticated: Ref<boolean>;
  isAdmin: Ref<boolean>;
  isLecturer: Ref<boolean>;
  isStudent: Ref<boolean>;
  isGuest: Ref<boolean>;
  user: Ref<Profile | null>;
  authStore: Ref<AuthStore | null>;
}

const isAuthenticated = computed(() => state.value.isAuthenticated);
const isAuthLoading = computed(() => state.value.isLoading);
const isUnauthenticated = computed(() => state.value.isUnauthenticated);
const isAdmin = computed(() => state.value.isAdmin);
const isLecturer = computed(() => state.value.isLecturer);
const isStudent = computed(() => state.value.isStudent);
const isGuest = computed(() => state.value.isGuest);
const user = computed(() => state.value.user);

export function useAuthStore(): UseAuthStore {
  return {
    state,
    logout,
    setStore,
    isAuthenticated,
    isAuthLoading,
    isUnauthenticated,
    isAdmin,
    isLecturer,
    isStudent,
    isGuest,
    user,
    authStore,
  };
}

export function initAuthStore() {
  preload();
}

export async function isAuthStoreReady() {
  if (isPreloadDone.value) {
    return true;
  }

  return new Promise((resolve) => {
    watchEffect(() => {
      if (isPreloadDone.value) {
        resolve(true);
      }
    });
  });
}