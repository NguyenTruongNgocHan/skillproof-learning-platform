import { describe, it, expect } from 'vitest';
import {
  checkPassword,
  isPasswordValid,
  getPasswordStrength,
} from '@/features/auth/validation/passwordPolicy';

describe('checkPassword', () => {
  it('passes all checks for a strong password', () => {
    const result = checkPassword('Str0ng!PassW0rd', 'user@example.com');
    expect(result.minLength).toBe(true);
    expect(result.maxLength).toBe(true);
    expect(result.uppercase).toBe(true);
    expect(result.lowercase).toBe(true);
    expect(result.number).toBe(true);
    expect(result.special).toBe(true);
    expect(result.noLeadingTrailingSpace).toBe(true);
    expect(result.notCommon).toBe(true);
  });

  it('fails minLength for a short password', () => {
    const result = checkPassword('Short1!A');
    expect(result.minLength).toBe(false);
  });

  it('fails maxLength for a password longer than 72 characters', () => {
    const result = checkPassword('A'.repeat(73) + '!1a');
    expect(result.maxLength).toBe(false);
  });

  it('fails uppercase when no uppercase letter', () => {
    const result = checkPassword('alllower1!password');
    expect(result.uppercase).toBe(false);
  });

  it('fails lowercase when no lowercase letter', () => {
    const result = checkPassword('ALLUPPER1!PASSWORD');
    expect(result.lowercase).toBe(false);
  });

  it('fails number when no digit', () => {
    const result = checkPassword('NoDigitsHere!Password');
    expect(result.number).toBe(false);
  });

  it('fails special when no special character', () => {
    const result = checkPassword('NoSpecialChar123A');
    expect(result.special).toBe(false);
  });

  it('fails noLeadingTrailingSpace for leading space', () => {
    const result = checkPassword(' Leading1!Password');
    expect(result.noLeadingTrailingSpace).toBe(false);
  });

  it('fails noLeadingTrailingSpace for trailing space', () => {
    const result = checkPassword('Trailing1!Password ');
    expect(result.noLeadingTrailingSpace).toBe(false);
  });

  it('flags common passwords', () => {
    const result = checkPassword('password123');
    expect(result.notCommon).toBe(false);
  });

  it('flags password that contains full email', () => {
    const result = checkPassword('user@example.comAbc1!', 'user@example.com');
    expect(result.notCommon).toBe(false);
  });
});

describe('isPasswordValid', () => {
  it('returns true when all checks pass', () => {
    const checks = checkPassword('Str0ng!PassW0rd', 'other@example.com');
    expect(isPasswordValid(checks)).toBe(true);
  });

  it('returns false when any check fails', () => {
    const checks = checkPassword('short1!A');
    expect(isPasswordValid(checks)).toBe(false);
  });
});

describe('getPasswordStrength', () => {
  it('returns weak for empty string', () => {
    expect(getPasswordStrength('')).toBe('weak');
  });

  it('returns weak for a very short password', () => {
    expect(getPasswordStrength('abc')).toBe('weak');
  });

  it('returns fair for a moderate password', () => {
    expect(getPasswordStrength('abcABC123')).toBe('fair');
  });

  it('returns strong for a good password', () => {
    expect(getPasswordStrength('abcABC123!!')).toBe('strong');
  });

  it('returns very-strong for a long complex password', () => {
    expect(getPasswordStrength('abcABC123!!!XYZ789')).toBe('very-strong');
  });
});
