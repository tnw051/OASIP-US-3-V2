<script setup lang="ts">
import { InteractionType } from "@azure/msal-browser";
import { computed, watch } from "vue";
import { useRouter } from "vue-router";
import { useIsAuthenticated } from "./composables/useIsAuthenticated";
import { useMsal } from "./composables/useMsal";
import { useMsalAuthentication } from "./composables/useMsalAuthentication";
import { tokenRequest } from "./configs/msalAuthConfig";
import { useAuth } from "./utils/useAuth";

const router = useRouter();

const { logout, isAdmin, isLecturer, isAuthenticated, user } = useAuth();

const { instance: msalInstance } = useMsal();
const isMsalAuthenticated = useIsAuthenticated();
const { result, isGuestMsal, isStudentMsal, isAdminMsal, isLecturerMsal, roleMsal } = useMsalAuthentication(InteractionType.Silent, tokenRequest);

const authState = computed(() => ({
  isAdmin: isAdmin.value || isAdminMsal.value,
  isLecturer: isLecturer.value || isLecturerMsal.value,
  isStudent: isStudentMsal.value || isStudentMsal.value,
  isGuest: !isAuthenticated.value || isGuestMsal.value,
  isAuthenticated: isAuthenticated.value || isMsalAuthenticated.value,
  name: user.value?.sub || result.value?.account?.name,
  role: user.value?.role || roleMsal.value,
}));

watch(result, () => {
  console.log(result.value);

});

async function handleLogout() {
  if (isAuthenticated.value) {
    console.log("Logging out OASIP");
    await logoutOasip();
    return;
  }

  if (isMsalAuthenticated.value) {
    console.log("Logging out MSAL");
    await msalInstance.logoutRedirect({
      logoutHint: result.value?.account.idTokenClaims.login_hint,
      onRedirectNavigate: (url) => {
        // Skipping the server sign-out for the sake of ease of development
        return false;
      },
    });
    return;
  }
}

async function logoutOasip() {
  const success = await logout();
  if (success) {
    router.push({ name: "login" });
  } else {
    alert("Something went wrong");
  }
}
</script>
 
<template>
  <nav class="flex items-center justify-between border-b border-gray-200 bg-white px-12 py-4">
    <button
      class="text-gray-600 hover:text-sky-600"
      @click="msalInstance.acquireTokenRedirect(tokenRequest)"
    >
      Acquire token
    </button>
    <div class="flex items-center gap-1 text-sm font-medium">
      <img
        src="https://cdn.7tv.app/emote/611cb0c5f20f644c3fadb992/3x"
        width="64"
      >
      <img
        src="https://cdn.betterttv.net/emote/60a21baf67644f1d67e87a6c/3x"
        width="64"
      >
      <router-link
        to="/"
        class="ml-6 rounded-md p-2 text-gray-700 hover:text-sky-600"
      >
        Events
      </router-link>
      <router-link
        v-if="!authState.isLecturer"
        :to="{ name: 'createEvent' }"
        class="rounded-md p-2 text-gray-700 hover:text-sky-600"
      >
        Create
        Event
      </router-link>
      <router-link
        :to="{ name: 'categories' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Categories
      </router-link>
      <router-link
        v-if="authState.isAdmin"
        :to="{ name: 'users' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Users
      </router-link>
      <router-link
        v-if="authState.isAdmin"
        :to="{ name: 'createUser' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Create User
      </router-link>
      <router-link
        v-if="!authState.isAuthenticated"
        :to="{ name: 'login' }"
        class="ml-2 p-2 text-gray-700 hover:text-sky-600"
      >
        Login
      </router-link>
    </div>
    <div class="flex items-center text-sm text-gray-700">
      <div class="flex flex-col text-xs font-medium">
        <span v-if="authState.isAuthenticated">{{ authState.name }}</span>
        <!-- <span
          v-if="authState.isGuest"
          class="text-gray-500"
        >Guest</span>
        <span
          v-else-if="authState.isAuthenticated"
          class="text-blue-400"
        >{{ authState.role }}</span> -->
      </div>
      <a
        v-if="authState.isAuthenticated"
        class="ml-4 cursor-pointer rounded-md border border-gray-200 p-2 text-gray-700 hover:text-sky-600"
        @click="handleLogout"
      >Logout</a>
    </div>
  </nav>

  <router-view />
</template>
 
<style scoped>
.router-link-active {
  @apply text-sky-600 bg-sky-50;
}
</style>