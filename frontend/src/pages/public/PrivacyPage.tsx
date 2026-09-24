import LegalLayout from "./LegalLayout";
export default function PrivacyPage() {
  return (
    <LegalLayout
      title="Privacy notice"
      intro="How the current SkillProof identity and organization flows use information you provide."
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
          headline, bio, avatar URL, locale and time zone.
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
          unauthorized access.
        </p>
      </section>
      <section>
        <h2>Sessions and browser data</h2>
        <p>
          The refresh session uses a browser cookie; the access token is held in
          memory. We store your display theme preference on your device. Some
          existing learner onboarding preferences may also be saved locally in
          the browser; do not treat browser data as authorization. Security
          audit records can contain your IP address and browser user agent.
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
          review details as certificate verification data. Future learning,
          payment, recommendation and certificate features require an updated
          notice before they are enabled.
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
  );
}
