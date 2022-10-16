import { reactive, watch, computed } from "vue";
import { UserResponse } from "../gen-types";
import { roles } from "./useAuth";
import { ValidationResult, makeValidateResult } from "./validators/common";

function validateName(name: string): ValidationResult {
  const errors: string[] = [];

  if (name.length > 100) {
    errors.push("Name must be less than 100 characters");
  }

  if (name.trim().length === 0) {
    errors.push("Name must not be blank");
  }

  return makeValidateResult(errors);
}

function validateEmail(email: string): ValidationResult {
  const errors: string[] = [];

  if (email.length > 50) {
    errors.push("Email must be less than 50 characters");
  }

  if (email.trim().length === 0) {
    errors.push("Email must not be blank");
  }

  // RFC2822 https://regexr.com/2rhq7
  const emailRegex = /^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$/;
  if (!emailRegex.test(email)) {
    errors.push("Email is invalid");
  }

  return makeValidateResult(errors);
}

function validatePassword(password: string): ValidationResult {
  const errors: string[] = [];

  if (password.length === 0) {
    errors.push("Password must not be blank");
  } else if (password.length > 14 || password.length < 8) {
    errors.push("Password must be between 8 and 14 characters");
  }

  return makeValidateResult(errors);
}

function validateConfirmPassword(password: string, confirmPassword: string): ValidationResult {
  const errors: string[] = [];

  if (confirmPassword !== password) {
    errors.push("Passwords do not match");
  }

  return makeValidateResult(errors);
}

interface Options {
  currentUser?: UserResponse;
}

export function useUserValidator(options?: Options) {
  const { currentUser } = options || {};
  const defaultTextValue = "";
  const defaultInputs = {
    name: currentUser?.name || defaultTextValue,
    email: currentUser?.email || defaultTextValue,
    password: defaultTextValue,
    confirmPassword: defaultTextValue,
    role: currentUser?.role || roles.STUDENT,
  };

  interface Errors {
    name: string[] | false;
    email: string[] | false;
    password: string[] | false;
    confirmPassword: string[] | false;
  }

  const defaultErrors: Errors = {
    name: false,
    email: false,
    password: false,
    confirmPassword: false,
  };

  const inputs = reactive({ ...defaultInputs });
  const errors = reactive({ ...defaultErrors });

  watch(() => inputs.name, (name) => {
    const result = validateName(name);
    errors.name = result.valid ? false : result.errors;
  });

  watch(() => inputs.email, (email) => {
    const result = validateEmail(email);
    errors.email = result.valid ? false : result.errors;
  });

  watch(() => inputs.password, (password) => {
    const result = validatePassword(password);
    errors.password = result.valid ? false : result.errors;

    if (inputs.confirmPassword !== defaultTextValue) {
      const result = validateConfirmPassword(password, inputs.confirmPassword);
      errors.confirmPassword = result.valid ? false : result.errors;
    }
  });

  watch(() => inputs.confirmPassword, (confirmPassword) => {
    if (inputs.password === defaultTextValue) {
      return;
    }

    const result = validateConfirmPassword(inputs.password, confirmPassword);
    errors.confirmPassword = result.valid ? false : result.errors;
  });

  const hasErrors = computed(() => {
    return Object.entries(errors).some(([key, value]) => {
      return value !== false;
    });
  });

  const filledIn = computed(() => {
    return Object.entries(inputs).every(([key, value]) => {
      if (key === "role") {
        return true;
      }

      return value !== defaultTextValue;
    });
  });

  const hasChanges = computed(() => {
    if (currentUser === undefined) {
      return false;
    }

    return inputs.name !== currentUser.name ||
      inputs.email !== currentUser.email ||
      inputs.role !== currentUser.role;
  });

  const canSubmit = computed(() => {
    if (currentUser) {
      return !hasErrors.value && hasChanges.value;
    }

    return !hasErrors.value && filledIn.value;
  });

  function resetInputsAndErrors() {
    Object.assign(inputs, defaultInputs);
    Object.assign(errors, defaultErrors);
  }

  return {
    errors,
    inputs,
    resetInputsAndErrors,
    hasErrors,
    hasChanges,
    canSubmit,
  };
}