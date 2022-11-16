import {
  AuthenticationResult,
  AuthError,
  InteractionStatus,
  InteractionType,
  PopupRequest,
  RedirectRequest,
  SilentRequest,
} from "@azure/msal-browser";
import { ref } from "vue";
import { useMsal } from "./useMsal";

export function useMsalAuthentication(interactionType: InteractionType, request: PopupRequest | RedirectRequest | SilentRequest) {
  const { instance, inProgress } = useMsal();

  const localInProgress = ref<boolean>(false);
  const result = ref<AuthenticationResult | null>(null);
  const error = ref<AuthError | null>(null);

  async function acquireToken(requestOverride?: PopupRequest | RedirectRequest | SilentRequest) {
    if (!localInProgress.value) {
      localInProgress.value = true;
      const tokenRequest = requestOverride || request;
      tokenRequest.account = instance.getAllAccounts()[0];

      if (inProgress.value === InteractionStatus.Startup || inProgress.value === InteractionStatus.HandleRedirect) {
        try {
          const response = await instance.handleRedirectPromise();

          if (response) {
            result.value = response;
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
        error.value = null;
      } catch (e) {
        if (inProgress.value !== InteractionStatus.None) {
          return;
        }

        if (interactionType === InteractionType.Popup) {
          instance.loginPopup(tokenRequest).then((response) => {
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
  }

  return {
    acquireToken,
    result,
    error,
    inProgress: localInProgress,
  };
}
