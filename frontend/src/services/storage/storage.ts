const KEYS = {
  AUTH_SESSION: 'skillproof-auth-session',
  THEME: 'skillproof-theme',
  LEARNER_PROFILE: 'skillproof-learner-profile',
  ORGANIZER_PROFILE: 'skillproof-organizer-profile',
} as const;

export type StorageKey = (typeof KEYS)[keyof typeof KEYS];

const storage = {
  get<T>(key: StorageKey): T | null {
    try {
      const raw = localStorage.getItem(key);
      if (!raw) return null;
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  },
  set<T>(key: StorageKey, value: T): void {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch {
      // storage full or blocked — silently fail
    }
  },
  remove(key: StorageKey): void {
    try {
      localStorage.removeItem(key);
    } catch { /* silent */ }
  },
};

export { storage, KEYS };
