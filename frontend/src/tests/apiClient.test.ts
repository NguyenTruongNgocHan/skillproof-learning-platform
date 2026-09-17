import { afterEach, describe, expect, it, vi } from 'vitest';
import { apiClient, setAccessToken } from '@/services/api/apiClient';

function response(status: number, body: unknown) {
  return new Response(JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('apiClient', () => {
  afterEach(() => {
    setAccessToken(null);
    vi.restoreAllMocks();
  });

  it('preserves login errors without attempting refresh', async () => {
    const fetchMock = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      response(401, {
        code: 'IDENTITY_INVALID_CREDENTIALS',
        message: 'Email or password is incorrect.',
      }),
    );

    await expect(
      apiClient.post('/auth/login', {
        email: 'learner@example.com',
        password: 'wrong',
      }),
    ).rejects.toMatchObject({
      status: 401,
      problem: { code: 'IDENTITY_INVALID_CREDENTIALS' },
    });

    expect(fetchMock).toHaveBeenCalledTimes(1);
  });

  it('refreshes once and retries an authenticated request', async () => {
    setAccessToken('expired-access-token');

    const fetchMock = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(response(401, {
        code: 'IDENTITY_UNAUTHORIZED',
        message: 'Authentication is required.',
      }))
      .mockResolvedValueOnce(response(200, {
        accessToken: 'new-access-token',
        expiresIn: 900,
        user: {
          id: 'user-1',
          email: 'learner@example.com',
          displayName: 'Learner',
          role: 'LEARNER',
        },
      }))
      .mockResolvedValueOnce(response(200, { email: 'learner@example.com' }));

    await expect(apiClient.get('/me')).resolves.toEqual({
      email: 'learner@example.com',
    });
    expect(fetchMock).toHaveBeenCalledTimes(3);
  });
});
