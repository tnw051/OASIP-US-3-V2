import { makeValidateResult, ValidationResult } from "./common";

export function validateName(name: string): ValidationResult {
  const errors = [];

  if (name.length > 100) {
    errors.push("Name must be less than 100 characters");
  }

  if (name.trim().length === 0) {
    errors.push("Name must not be blank");
  }

  return makeValidateResult(errors);
}


export function validateEmail(email: string): ValidationResult {
  const errors = [];

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

export function validatePassword(password: string): ValidationResult {
  const errors = [];

  if (password.length > 14 || password.length < 8) {
    errors.push("Password must be between 8 and 14 characters");
  }

  if (password.length === 0) {
    errors.push("Password must not be blank");
  }

  return makeValidateResult(errors);
}

export function validateConfirmPassword(password: string, confirmPassword: string): ValidationResult {
  const errors = [];

  if (confirmPassword !== password) {
    errors.push("Passwords do not match");
  }

  return makeValidateResult(errors);
}