import LegalLayout from "./LegalLayout"
export default function PrivacyPage() {
  return (
    <LegalLayout
      title="Privacy notice"
      intro="Review draft: information used by account, organization and learning flows."
    >
      <section>
        <h2>Information used today</h2>
        <p>
          When you create an account, we use your email, display name, password
          hash, account role and verification state. Organizer applications
          additionally contain the organization name, industry, country,
          registration number (if supplied), contact name, contact email,
          website and phone (if supplied). Administrators can record an approval
          decision and reason. Profile fields you choose to save may include a
          headline, bio, avatar URL, locale and time zone. If you enroll in a
          learning path, we also store the path version, resource completions,
          assessment attempts, selected answers, scores and completion state.
          Learner onboarding choices are stored with your account.
          Uploaded profile images, organization documents and lesson files may
          contain personal information or third party content.
        </p>
      </section>
      <section>
        <h2>Uploaded files</h2>
        <p>
          File bytes are kept in a private object storage bucket configured by
          the platform operator. The database records their owner, linked
          organization or lesson, file type, size and integrity hash. Access to
          the files goes through the application after an account and permission
          check. The operator must publish the actual storage provider, data
          location, backup and deletion policy before opening the service to
          the public.
        </p>
      </section>
      <section>
        <h2>Why we use it</h2>
        <p>
          We use account information to authenticate you, send verification and
          password recovery email, protect sessions, show your profile and
          control access. We use organization application details to review
          whether an organization can act as an official organizer. We record
          security and review events to investigate changes and prevent
          unauthorized access. Learning records let you resume your enrolled
          version, review progress and receive assessment feedback.
        </p>
      </section>
      <section>
        <h2>Sessions and browser data</h2>
        <p>
          The refresh session uses a browser cookie; the access token is held in
          memory. We store your display theme preference on your device. Some
          learner preferences on the backend; browser data is not authorization.
          Security audit records can contain your IP address and browser user
          agent.
        </p>
      </section>
      <section>
        <h2>Google sign-in and email delivery</h2>
        <p>
          If you choose Google sign-in, the authentication provider returns
          account identity information for sign-in. Email verification and
          recovery messages are sent using the configured mail service. The
          actual provider and processing locations depend on the deployment
          configuration and must be identified by the operator before launch.
        </p>
      </section>
      <section>
        <h2>Access and sharing</h2>
        <p>
          Authorized administrators can review accounts and organization
          applications. Approved organization members see information needed for
          their assigned permissions. We do not publish private registration and
          review details as certificate verification data. Learning attempts and
          answers are currently returned only to the Learner who owns them;
          access to organization-wide learner results needs a separate reviewed
          feature. Payment, recommendation and certificate features require an
          updated notice before they are enabled.
        </p>
      </section>
      <section>
        <h2>Control and retention</h2>
        <p>
          You can edit supported profile fields and sign out. Account deletion,
          data export, retention periods and a dedicated request channel must be
          implemented and documented before public launch. Until then, contact
          the project operator through the support channel supplied with your
          deployment to request access or correction.
        </p>
      </section>
      <section>
        <h2>Policy updates</h2>
        <p>
          This text describes the current implementation and is a review draft.
          Material changes to data processing should be reflected here before
          they go live.
        </p>
      </section>
    </LegalLayout>
  )
}
