import { ref } from "vue";

export function useIsLoading(val) {
  const isLoading = ref(val || false);

  function setIsLoading(val) {
    isLoading.value = val;
  }

  return {
    isLoading,
    setIsLoading
  };
}