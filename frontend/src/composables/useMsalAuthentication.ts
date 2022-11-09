import {
  AuthenticationResult,
  AuthError,
  InteractionStatus,
  InteractionType,
  PopupRequest,
  RedirectRequest,
  SilentRequest,
} from "@azure/msal-browser";
import { ref, watch } from "vue";
import { useMsal } from "./useMsal";

export function useMsalAuthentication(interactionType: InteractionType, request: PopupRequest | RedirectRequest | SilentRequest) {
  const { instance, inProgress } = useMsal();

  const localInProgress = ref<boolean>(false);
  const result = ref<AuthenticationResult | null>(null);
  const error = ref<AuthError | null>(null);

  const isAdminMsal = ref<boolean>(false);
  const isLecturerMsal = ref<boolean>(false);
  const isStudentMsal = ref<boolean>(false);
  const isGuestMsal = ref<boolean>(false);
  const roleMsal = ref<string>("");

  const acquireToken = async (requestOverride?: PopupRequest | RedirectRequest | SilentRequest) => {
    if (!localInProgress.value) {
      localInProgress.value = true;
      const tokenRequest = requestOverride || request;
      tokenRequest.account = instance.getAllAccounts()[0];
      console.log(`Acquiring token for ${tokenRequest.scopes}`);
      

      if (inProgress.value === InteractionStatus.Startup || inProgress.value === InteractionStatus.HandleRedirect) {
        try {
          const response = await instance.handleRedirectPromise();

          if (response) {
            result.value = response;
            setRoleRefsFromAuthenticationResult(response);
            error.value = null;
            return;
          }
        } catch (e) {
          result.value = null;
          error.value = e as AuthError;
          return;
        }
      }

      try {
        const response = await instance.acquireTokenSilent(tokenRequest);
        result.value = response;
        setRoleRefsFromAuthenticationResult(response);
        error.value = null;
      } catch (e) {
        if (inProgress.value !== InteractionStatus.None) {
          return;
        }

        if (interactionType === InteractionType.Popup) {
          instance.loginPopup(tokenRequest).then((response) => {
            setRoleRefsFromAuthenticationResult(response);
            result.value = response;
            error.value = null;
          }).catch((e) => {
            error.value = e;
            result.value = null;
          });
        } else if (interactionType === InteractionType.Redirect) {
          await instance.loginRedirect(tokenRequest).catch((e) => {
            error.value = e;
            result.value = null;
          });
        }
      }
      localInProgress.value = false;
    }
  };

  function setRoleRefsFromAuthenticationResult(result: AuthenticationResult) {
    const roles = result.account.idTokenClaims.roles;
    let roleCount = 0;

    if (roles.includes("Admin")) {
      isAdminMsal.value = true;
      roleCount++;
    }
    if (roles.includes("Lecturer")) {
      isLecturerMsal.value = true;
      roleCount++;
    }
    if (roles.includes("Student")) {
      isStudentMsal.value = true;
      roleCount++;
    }
    if (roleCount === 0) {
      isGuestMsal.value = true;
    }

    roleMsal.value = roles[0];
  }

  const stopWatcher = watch(inProgress, () => {
    if (!result.value && !error.value) {
      acquireToken();
    } else {
      stopWatcher();
    }
  });

  acquireToken();

  return {
    acquireToken,
    result,
    error,
    inProgress: localInProgress,
    isAdminMsal,
    isLecturerMsal,
    isStudentMsal,
    isGuestMsal,
    roleMsal,
  };
}