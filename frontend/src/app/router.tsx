import { lazy, Suspense } from "react"
import { Routes, Route } from "react-router-dom"
import { ROUTES } from "@/config/appRoutes"
import { AuthGuard } from "@/app/guards/AuthGuard"
import { GuestGuard } from "@/app/guards/GuestGuard"
import { RoleGuard } from "@/app/guards/RoleGuard"
import { OrganizerVerificationGuard } from "@/app/guards/OrganizerVerificationGuard"
import { OnboardingGuard } from "@/app/guards/OnboardingGuard"

import ScrollToRoute from "@/components/layout/ScrollToRoute"
import PrivacyPage from "@/pages/public/PrivacyPage"
import TermsPage from "@/pages/public/TermsPage"
import LandingPage from "@/pages/public/LandingPage"
import VerifyPage from "@/pages/public/VerifyPage"
import NotFoundPage from "@/pages/public/NotFoundPage"
import CommunityPage from "@/pages/public/CommunityPage"
import ComingSoonPage from "@/pages/public/ComingSoonPage"

import LoginPage from "@/pages/auth/LoginPage"
import RegisterPage from "@/pages/auth/RegisterPage"
import VerifyEmailPage from "@/pages/auth/VerifyEmailPage"
import OAuthCallbackPage from "@/pages/auth/OAuthCallbackPage"
import ForgotPasswordPage from "@/pages/auth/ForgotPasswordPage"
import ResetPasswordPage from "@/pages/auth/ResetPasswordPage"

import LearnerOnboardingPage from "@/pages/onboarding/LearnerOnboardingPage"

import DashboardPage from "@/pages/learner/DashboardPage"
import AdminPage from "@/pages/admin/AdminPage"
import ProfilePage from "@/pages/profile/ProfilePage"
import ExplorePathsPage from "@/pages/learning/ExplorePathsPage"
import PathDetailPage from "@/pages/learning/PathDetailPage"
import MyLearningPage from "@/pages/learning/MyLearningPage"
import CoursePage from "@/pages/learning/CoursePage"
import AttemptPage from "@/pages/quiz/AttemptPage"
import PracticeHubPage from "@/pages/quiz/PracticeHubPage"
import ChallengePage from "@/pages/realtime/ChallengePage"
import EligibilityPage from "@/pages/eligibility/EligibilityPage"

const OrganizerOnboardingPage = lazy(
  () => import("@/pages/onboarding/OrganizerOnboardingPage"),
)
const OrganizerPage = lazy(() => import("@/pages/organizer/OrganizerPage"))
const OrganizerVerificationPendingPage = lazy(
  () => import("@/pages/organizer/OrganizerVerificationPendingPage"),
)
const OrganizerPathsPage = lazy(
  () => import("@/pages/learning/OrganizerPathsPage"),
)
const PathStudioPage = lazy(() => import("@/pages/learning/PathStudioPage"))
const QuestionBankPage = lazy(() => import("@/pages/quiz/QuestionBankPage"))
const CertificationProgramsPage = lazy(
  () => import("@/pages/organizer/CertificationProgramsPage"),
)
const AcceptInvitationPage = lazy(
  () => import("@/pages/organizer/AcceptInvitationPage"),
)
const OrganizationWorkspacePage = lazy(
  () => import("@/pages/organizer/OrganizationWorkspacePage"),
)
const CertificateDetailPage = lazy(
  () => import("@/pages/organizer/CertificateDetailPage"),
)
const AdminOrganizationReviewPage = lazy(
  () => import("@/pages/admin/AdminOrganizationReviewPage"),
)

export function AppRouter() {
  return (
    <>
      <ScrollToRoute />
      <Suspense
        fallback={
          <main className="sporg" role="status">
            Loading workspace…
          </main>
        }
      >
        <Routes>
          {/* Public */}
          <Route path={ROUTES.HOME} element={<LandingPage />} />
          <Route path={ROUTES.COMMUNITY} element={<CommunityPage />} />
          <Route path={ROUTES.VERIFY_CERT} element={<VerifyPage />} />
          <Route
            path={`${ROUTES.VERIFY_CERT}/:certificateId`}
            element={<VerifyPage />}
          />
          <Route
            path={ROUTES.EXPLORE}
            element={
              <ComingSoonPage
                title="Explore"
                description="Discover learning paths, quizzes, and challenges."
              />
            }
          />
          <Route path={ROUTES.LEARNING_PATHS} element={<ExplorePathsPage />} />
          <Route path="/learning-paths/:id" element={<PathDetailPage />} />
          <Route path={ROUTES.PRACTICE} element={<PracticeHubPage />} />
          <Route
            path={ROUTES.CERTIFICATIONS}
            element={
              <ComingSoonPage
                title="Certifications"
                description="Browse certification programs."
              />
            }
          />

          <Route path={ROUTES.PRIVACY} element={<PrivacyPage />} />
          <Route path={ROUTES.TERMS} element={<TermsPage />} />

          {/* Guest-only auth pages */}
          <Route
            path={ROUTES.LOGIN}
            element={
              <GuestGuard>
                <LoginPage />
              </GuestGuard>
            }
          />
          <Route
            path={ROUTES.REGISTER}
            element={
              <GuestGuard>
                <RegisterPage />
              </GuestGuard>
            }
          />
          <Route
            path={ROUTES.FORGOT_PASSWORD}
            element={
              <GuestGuard>
                <ForgotPasswordPage />
              </GuestGuard>
            }
          />
          <Route path={ROUTES.RESET_PASSWORD} element={<ResetPasswordPage />} />
          <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
          <Route
            path="/profile"
            element={
              <AuthGuard>
                <ProfilePage />
              </AuthGuard>
            }
          />

          {/* Email verification — auth required, email NOT yet verified */}
          <Route path={ROUTES.VERIFY_EMAIL} element={<VerifyEmailPage />} />

          {/* Onboarding */}
          <Route
            path={ROUTES.ONBOARDING_LEARNER}
            element={
              <AuthGuard>
                <RoleGuard allowedRole="LEARNER">
                  <LearnerOnboardingPage />
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path={ROUTES.ONBOARDING_ORGANIZER}
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerOnboardingPage />
                </RoleGuard>
              </AuthGuard>
            }
          />

          {/* Learner app */}
          <Route
            path={ROUTES.LEARNER_APP}
            element={
              <AuthGuard>
                <OnboardingGuard>
                  <RoleGuard allowedRole="LEARNER">
                    <DashboardPage />
                  </RoleGuard>
                </OnboardingGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/app/learning"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="LEARNER">
                  <MyLearningPage />
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/app/learning/:id"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="LEARNER">
                  <CoursePage />
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/app/attempts/:id"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="LEARNER">
                  <AttemptPage />
                </RoleGuard>
              </AuthGuard>
            }
          />

          <Route
            path={ROUTES.CHALLENGE}
            element={
              <AuthGuard>
                <RoleGuard allowedRole="LEARNER">
                  <ChallengePage />
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path={ROUTES.ELIGIBILITY}
            element={
              <AuthGuard>
                <RoleGuard allowedRole="LEARNER">
                  <EligibilityPage />
                </RoleGuard>
              </AuthGuard>
            }
          />

          {/* Organizer */}
          <Route
            path={ROUTES.ORGANIZER_PENDING}
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationPendingPage />
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path={ROUTES.ORGANIZER_APP}
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <OrganizerPage />
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/paths"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <OrganizerPathsPage />
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/paths/:id"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <PathStudioPage />
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/questions"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <QuestionBankPage />
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/certifications"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <CertificationProgramsPage />
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/invitations/:token/accept"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <AcceptInvitationPage />
                </RoleGuard>
              </AuthGuard>
            }
          />

          <Route
            path="/organizer/organization"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <OrganizationWorkspacePage />
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/certificates/:id"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <CertificateDetailPage />
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/admin/organizations"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ADMIN">
                  <AdminOrganizationReviewPage />
                </RoleGuard>
              </AuthGuard>
            }
          />

          {/* Admin */}
          <Route
            path={ROUTES.ADMIN}
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ADMIN">
                  <AdminPage />
                </RoleGuard>
              </AuthGuard>
            }
          />

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </>
  )
}
