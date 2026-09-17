import { Routes, Route } from 'react-router-dom';
import { ROUTES } from '@/config/appRoutes';
import { AuthGuard } from '@/app/guards/AuthGuard';
import { GuestGuard } from '@/app/guards/GuestGuard';
import { RoleGuard } from '@/app/guards/RoleGuard';
import { OnboardingGuard } from '@/app/guards/OnboardingGuard';

import LandingPage from '@/pages/public/LandingPage';
import VerifyPage from '@/pages/public/VerifyPage';
import NotFoundPage from '@/pages/public/NotFoundPage';
import CommunityPage from '@/pages/public/CommunityPage';
import ComingSoonPage from '@/pages/public/ComingSoonPage';

import LoginPage from '@/pages/auth/LoginPage';
import RegisterPage from '@/pages/auth/RegisterPage';
import VerifyEmailPage from '@/pages/auth/VerifyEmailPage';
import OAuthCallbackPage from '@/pages/auth/OAuthCallbackPage';
import ForgotPasswordPage from '@/pages/auth/ForgotPasswordPage';
import ResetPasswordPage from '@/pages/auth/ResetPasswordPage';

import LearnerOnboardingPage from '@/pages/onboarding/LearnerOnboardingPage';
import OrganizerOnboardingPage from '@/pages/onboarding/OrganizerOnboardingPage';

import DashboardPage from '@/pages/learner/DashboardPage';
import OrganizerPage from '@/pages/organizer/OrganizerPage';
import OrganizerVerificationPendingPage from '@/pages/organizer/OrganizerVerificationPendingPage';
import AdminPage from '@/pages/admin/AdminPage';
import ProfilePage from '@/pages/profile/ProfilePage';

export function AppRouter() {
  return (
    <Routes>
      {/* Public */}
      <Route path={ROUTES.HOME} element={<LandingPage />} />
      <Route path={ROUTES.COMMUNITY} element={<CommunityPage />} />
      <Route path={ROUTES.VERIFY_CERT} element={<VerifyPage />} />
      <Route path={`${ROUTES.VERIFY_CERT}/:certificateId`} element={<VerifyPage />} />
      <Route path={ROUTES.EXPLORE} element={<ComingSoonPage title="Explore" description="Discover learning paths, quizzes, and challenges." />} />
      <Route path={ROUTES.LEARNING_PATHS} element={<ComingSoonPage title="Learning Paths" description="Browse all structured learning paths." />} />
      <Route path={ROUTES.PRACTICE} element={<ComingSoonPage title="Practice" description="Quizzes, mock tests and realtime challenges." />} />
      <Route path={ROUTES.CERTIFICATIONS} element={<ComingSoonPage title="Certifications" description="Browse certification programs." />} />

      {/* Guest-only auth pages */}
      <Route path={ROUTES.LOGIN} element={<GuestGuard><LoginPage /></GuestGuard>} />
      <Route path={ROUTES.REGISTER} element={<GuestGuard><RegisterPage /></GuestGuard>} />
      <Route path={ROUTES.FORGOT_PASSWORD} element={<GuestGuard><ForgotPasswordPage /></GuestGuard>} />
      <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />
      <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
      <Route path="/profile" element={<AuthGuard><ProfilePage /></AuthGuard>} />

      {/* Email verification — auth required, email NOT yet verified */}
      <Route path={ROUTES.VERIFY_EMAIL} element={<VerifyEmailPage />} />

      {/* Onboarding */}
      <Route path={ROUTES.ONBOARDING_LEARNER} element={<AuthGuard><RoleGuard allowedRole="LEARNER"><LearnerOnboardingPage /></RoleGuard></AuthGuard>} />
      <Route path={ROUTES.ONBOARDING_ORGANIZER} element={<AuthGuard><RoleGuard allowedRole="ORGANIZER"><OrganizerOnboardingPage /></RoleGuard></AuthGuard>} />

      {/* Learner app */}
      <Route path={ROUTES.LEARNER_APP} element={<AuthGuard><OnboardingGuard><RoleGuard allowedRole="LEARNER"><DashboardPage /></RoleGuard></OnboardingGuard></AuthGuard>} />
      <Route path={`${ROUTES.LEARNER_APP}/*`} element={<AuthGuard><OnboardingGuard><RoleGuard allowedRole="LEARNER"><DashboardPage /></RoleGuard></OnboardingGuard></AuthGuard>} />

      {/* Organizer */}
      <Route path={ROUTES.ORGANIZER_PENDING} element={<AuthGuard><RoleGuard allowedRole="ORGANIZER"><OrganizerVerificationPendingPage /></RoleGuard></AuthGuard>} />
      <Route path={ROUTES.ORGANIZER_APP} element={<AuthGuard><OnboardingGuard><RoleGuard allowedRole="ORGANIZER"><OrganizerPage /></RoleGuard></OnboardingGuard></AuthGuard>} />
      <Route path={`${ROUTES.ORGANIZER_APP}/*`} element={<AuthGuard><OnboardingGuard><RoleGuard allowedRole="ORGANIZER"><OrganizerPage /></RoleGuard></OnboardingGuard></AuthGuard>} />

      {/* Admin */}
      <Route path={ROUTES.ADMIN} element={<AuthGuard><RoleGuard allowedRole="ADMIN"><AdminPage /></RoleGuard></AuthGuard>} />
      <Route path={`${ROUTES.ADMIN}/*`} element={<AuthGuard><RoleGuard allowedRole="ADMIN"><AdminPage /></RoleGuard></AuthGuard>} />

      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
