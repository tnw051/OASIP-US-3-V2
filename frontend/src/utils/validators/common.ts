export type ValidationResult<Extra extends object = Record<string, unknown>> = {
  valid: boolean;
  errors: string[];
} & Extra;

export function makeValidateResult(errors: string[]): ValidationResult {
  return {
    valid: errors.length === 0,
    errors,
  };
}
