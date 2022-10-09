export interface ValidationResult {
  valid: boolean;
  errors: string[];
}

export function makeValidateResult(errors: string[]): ValidationResult {
  return {
    valid: errors.length === 0,
    errors,
  };
}
