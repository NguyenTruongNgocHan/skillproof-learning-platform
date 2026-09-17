import { describe, it, expect } from 'vitest';
import {
  getDefaultRouteForRole,
  getNextRouteAfterLogin,
  getNextRouteAfterVerification,
  canAccessRole,
} from '@/utils/authFlow';
import type { User } from '@/features/auth/types/auth.types';

function makeUser(overrides: Partial<User> = {}): User {
  return {
    id: 'test-1',
    email: 'test@example.com',
    fullName: 'Test User',
    role: 'LEARNER',
    emailVerificationStatus: 'VERIFIED',
    onboardingStatus: 'COMPLETED',
    ...overrides,
  };
}

describe('getDefaultRouteForRole', () => {
  it('returns /app for LEARNER', () => {
    expect(getDefaultRouteForRole('LEARNER')).toBe('/app');
  });
  it('returns /organizer for ORGANIZER', () => {
    expect(getDefaultRouteForRole('ORGANIZER')).toBe('/organizer');
  });
  it('returns /admin for ADMIN', () => {
    expect(getDefaultRouteForRole('ADMIN')).toBe('/admin');
  });
});

describe('getNextRouteAfterLogin', () => {
  it('redirects to /verify-email if email is UNVERIFIED', () => {
    const user = makeUser({ emailVerificationStatus: 'UNVERIFIED' });
    expect(getNextRouteAfterLogin(user)).toBe('/verify-email');
  });

  it('redirects learner with incomplete onboarding to /onboarding/learner', () => {
    const user = makeUser({ onboardingStatus: 'NOT_STARTED' });
    expect(getNextRouteAfterLogin(user)).toBe('/onboarding/learner');
  });

  it('redirects completed learner to /app', () => {
    const user = makeUser();
    expect(getNextRouteAfterLogin(user)).toBe('/app');
  });

  it('redirects organizer with incomplete onboarding to /onboarding/organizer', () => {
    const user = makeUser({ role: 'ORGANIZER', onboardingStatus: 'NOT_STARTED' });
    expect(getNextRouteAfterLogin(user)).toBe('/onboarding/organizer');
  });

  it('redirects completed organizer to /organizer', () => {
    const user = makeUser({ role: 'ORGANIZER' });
    expect(getNextRouteAfterLogin(user)).toBe('/organizer');
  });

  it('redirects admin to /admin', () => {
    const user = makeUser({ role: 'ADMIN' });
    expect(getNextRouteAfterLogin(user)).toBe('/admin');
  });
});

describe('getNextRouteAfterVerification', () => {
  it('sends learner to onboarding if NOT_STARTED', () => {
    const user = makeUser({ onboardingStatus: 'NOT_STARTED' });
    expect(getNextRouteAfterVerification(user)).toBe('/onboarding/learner');
  });

  it('sends learner with COMPLETED onboarding to /app', () => {
    const user = makeUser();
    expect(getNextRouteAfterVerification(user)).toBe('/app');
  });

  it('sends organizer to onboarding if NOT_STARTED', () => {
    const user = makeUser({ role: 'ORGANIZER', onboardingStatus: 'NOT_STARTED' });
    expect(getNextRouteAfterVerification(user)).toBe('/onboarding/organizer');
  });
});

describe('canAccessRole', () => {
  it('returns false for null user', () => {
    expect(canAccessRole(null, 'LEARNER')).toBe(false);
  });

  it('returns true when user role matches', () => {
    const user = makeUser({ role: 'ADMIN' });
    expect(canAccessRole(user, 'ADMIN')).toBe(true);
  });

  it('returns false when role does not match', () => {
    const user = makeUser({ role: 'LEARNER' });
    expect(canAccessRole(user, 'ADMIN')).toBe(false);
  });
});
