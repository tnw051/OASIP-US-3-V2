import { CategoryResponse } from "../../gen-types";
import { makeValidateResult, ValidationResult } from "./common";

function isNameUnique(name: string, categories: CategoryResponse[]) {
  const existingCategory = categories.find((category) => category.eventCategoryName.toLowerCase() === name.trim().toLowerCase() && category.id != category.id);
  if (existingCategory) {
    return false;
  }

  return true;
}

export function validateName(name: string, categories: CategoryResponse[]): ValidationResult {
  const errors = [];

  if (name.length > 100) {
    errors.push("Category name must be less than 100 characters");
  }

  if (name.trim().length === 0) {
    errors.push("Category name must not be blank");
  }

  if (!isNameUnique(name, categories)) {
    errors.push("Category name is not unique");
  }

  return makeValidateResult(errors);
}

export function validateDuration(duration: number): ValidationResult {
  const errors = [];

  if (duration < 1 || duration > 480) {
    errors.push("Category duration must be between 1 and 480 minutes");
  }

  return makeValidateResult(errors);
}

export function validateDescription(description: string): ValidationResult {
  const errors = [];

  if (description.length > 500) {
    errors.push("Category descriptions must be less than 500 characters");
  }

  return makeValidateResult(errors);
}