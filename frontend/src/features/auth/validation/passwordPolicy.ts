const COMMON_PASSWORDS = ['password', 'password123', '12345678', 'qwerty', 'admin123', 'welcome123', '123456789012'];

export interface PasswordCheckResult {
  minLength: boolean;
  maxLength: boolean;
  uppercase: boolean;
  lowercase: boolean;
  number: boolean;
  special: boolean;
  noLeadingTrailingSpace: boolean;
  notCommon: boolean;
}

export type PasswordStrength = 'weak' | 'fair' | 'strong' | 'very-strong';

export function checkPassword(password: string, email?: string, fullName?: string): PasswordCheckResult {
  const lower = password.toLowerCase();
  return {
    minLength: password.length >= 12,
    maxLength: password.length <= 72,
    uppercase: /[A-Z]/.test(password),
    lowercase: /[a-z]/.test(password),
    number: /[0-9]/.test(password),
    special: /[!@#$%^&*()\-_=+\[\]{};:,.<>?/]/.test(password),
    noLeadingTrailingSpace: password === password.trim() && password.length > 0,
    notCommon: !COMMON_PASSWORDS.includes(lower) &&
      !(email && lower.includes(email.toLowerCase())) &&
      !(fullName && fullName.split(' ').some((part) => part.length > 3 && lower.includes(part.toLowerCase()))),
  };
}

export function isPasswordValid(result: PasswordCheckResult): boolean {
  return Object.values(result).every(Boolean);
}

export function getPasswordStrength(password: string): PasswordStrength {
  if (password.length === 0) return 'weak';
  let score = 0;
  if (password.length >= 12) score++;
  if (password.length >= 16) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[a-z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[!@#$%^&*()\-_=+\[\]{};:,.<>?/]/.test(password)) score++;
  if (score <= 2) return 'weak';
  if (score <= 3) return 'fair';
  if (score <= 5) return 'strong';
  return 'very-strong';
}
