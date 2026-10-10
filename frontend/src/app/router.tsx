import { OrganizerAuthorityGuard } from "@/app/guards/OrganizerAuthorityGuard";
import { ROUTES } from "@/app/config/appRoutes";
import { AuthGuard } from "@/app/guards/AuthGuard";
import { GuestGuard } from "@/app/guards/GuestGuard";
import { OnboardingGuard } from "@/app/guards/OnboardingGuard";
import { OrganizerVerificationGuard } from "@/app/guards/OrganizerVerificationGuard";
import { RoleGuard } from "@/app/guards/RoleGuard";
import { useAuth } from "@/features/auth/hooks/useAuth";
import { OrganizerSurface } from "@/features/organization/components/OrganizerSurface";
import { lazy, Suspense } from "react";
import { Route, Routes, useLocation, Navigate } from "react-router-dom";

import VerifyPage from "@/features/certification/pages/VerifyPage";
import CommunityPage from "@/features/community/pages/CommunityPage";
import ComingSoonPage from "@/features/marketing/pages/ComingSoonPage";
import LandingPage from "@/features/marketing/pages/LandingPage";
import NotFoundPage from "@/features/marketing/pages/NotFoundPage";
import PrivacyPage from "@/features/marketing/pages/PrivacyPage";
import TermsPage from "@/features/marketing/pages/TermsPage";
import ScrollToRoute from "@/shared/components/layout/ScrollToRoute";

import ForgotPasswordPage from "@/features/auth/pages/ForgotPasswordPage";
import LoginPage from "@/features/auth/pages/LoginPage";
import OAuthCallbackPage from "@/features/auth/pages/OAuthCallbackPage";
import RegisterPage from "@/features/auth/pages/RegisterPage";
import ResetPasswordPage from "@/features/auth/pages/ResetPasswordPage";
import VerifyEmailPage from "@/features/auth/pages/VerifyEmailPage";

import LearnerOnboardingPage from "@/features/profile/pages/LearnerOnboardingPage";

import AdminPage from "@/features/admin/pages/AdminPage";
import DashboardPage from "@/features/dashboard/pages/DashboardPage";
import EligibilityPage from "@/features/eligibility/pages/EligibilityPage";
import CoursePage from "@/features/learning/pages/CoursePage";
import ExplorePathsPage from "@/features/learning/pages/ExplorePathsPage";
import MyLearningPage from "@/features/learning/pages/MyLearningPage";
import PathDetailPage from "@/features/learning/pages/PathDetailPage";
import ProfilePage from "@/features/profile/pages/ProfilePage";
import AttemptPage from "@/features/quiz/pages/AttemptPage";
import PracticeHubPage from "@/features/quiz/pages/PracticeHubPage";
import ChallengePage from "@/features/realtime/pages/ChallengePage";

const OrganizerOnboardingPage = lazy(
  () => import("@/features/organization/pages/OrganizerOnboardingPage"),
);
const OrganizerPage = lazy(
  () => import("@/features/organization/pages/OrganizerPage"),
);
const OrganizerVerificationPendingPage = lazy(
  () =>
    import("@/features/organization/pages/OrganizerVerificationPendingPage"),
);
const OrganizerCoursesPage = lazy(
  () => import("@/features/course/pages/OrganizerCoursesPage"),
);
const CourseStudioPage = lazy(
  () => import("@/features/course/pages/CourseStudioPage"),
);
const QuestionBankPage = lazy(
  () => import("@/features/quiz/pages/QuestionBankPage"),
);
const CertificationProgramsPage = lazy(
  () => import("@/features/certification/pages/CertificationProgramsPage"),
);
const AcceptInvitationPage = lazy(
  () => import("@/features/organization/pages/AcceptInvitationPage"),
);
const OrganizationWorkspacePage = lazy(
  () => import("@/features/organization/pages/OrganizationWorkspacePage"),
);
const CertificateDetailPage = lazy(
  () => import("@/features/certification/pages/CertificateDetailPage"),
);
const AdminOrganizationReviewPage = lazy(
  () => import("@/features/admin/pages/AdminOrganizationReviewPage"),
);

function RouteLoadingFrame() {
  const { user } = useAuth();
  const { pathname } = useLocation();
  if (user?.role === "ORGANIZER" && pathname.startsWith("/organizer")) {
    return (
      <OrganizerSurface title="Loading workspace…">
        <section className="sporg-card" role="status" aria-busy="true">
          <p>Loading this page…</p>
          <div className="sporg-skeleton" aria-hidden="true" />
        </section>
      </OrganizerSurface>
    );
  }
  return (
    <main className="sporg" role="status">
      Loading workspace…
    </main>
  );
}

const OrganizationLibraryPage = lazy(
  () => import("@/features/library/pages/OrganizationLibraryPage"),
);
const OrganizationAccessPage = lazy(
  () => import("@/features/access/pages/OrganizationAccessPage"),
);
function LegacyCourseRedirect() {
  const location = useLocation();
  return (
    <Navigate
      to={location.pathname.replace("/organizer/paths", "/organizer/courses")}
      replace
    />
  );
}
export function AppRouter() {
  return (
    <>
      <ScrollToRoute />
      <Suspense fallback={<RouteLoadingFrame />}>
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

          {/* Email verification Ã¢â‚¬â€ auth required, email NOT yet verified */}
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

          <Route path="/organizer/paths" element={<LegacyCourseRedirect />} />
          <Route
            path="/organizer/paths/:id"
            element={<LegacyCourseRedirect />}
          />
          <Route
            path="/organizer/library"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <OrganizerAuthorityGuard authority="MANAGE_CONTENT">
                      <OrganizationLibraryPage />
                    </OrganizerAuthorityGuard>
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/access"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <OrganizerAuthorityGuard authority="MANAGE_CONTENT">
                      <OrganizationAccessPage />
                    </OrganizerAuthorityGuard>
                  </OrganizerVerificationGuard>
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
            path="/organizer/courses"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <OrganizerAuthorityGuard authority="MANAGE_CONTENT">
                      <OrganizerCoursesPage />
                    </OrganizerAuthorityGuard>
                  </OrganizerVerificationGuard>
                </RoleGuard>
              </AuthGuard>
            }
          />
          <Route
            path="/organizer/courses/:id"
            element={
              <AuthGuard>
                <RoleGuard allowedRole="ORGANIZER">
                  <OrganizerVerificationGuard>
                    <OrganizerAuthorityGuard authority="MANAGE_CONTENT">
                      <CourseStudioPage />
                    </OrganizerAuthorityGuard>
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
                    <OrganizerAuthorityGuard authority="MANAGE_CONTENT">
                      <QuestionBankPage />
                    </OrganizerAuthorityGuard>
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
                    <OrganizerAuthorityGuard authority="ISSUE_CERTIFICATES">
                      <CertificationProgramsPage />
                    </OrganizerAuthorityGuard>
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
  );
}
