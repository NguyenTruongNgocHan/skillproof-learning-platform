import LegalLayout from "@/features/marketing/pages/LegalLayout"
export default function TermsPage() {
  return (
    <LegalLayout
      title="Terms of use"
      intro="Rules for accessing SkillProof while the learning platform is in development."
    >
      <section>
        <h2>Accounts</h2>
        <p>
          Use accurate account and organization information. Keep your sign-in credentials private.
          Account access may be disabled when required to protect the service or investigate misuse.
          An Organizer account alone does not authorize official publishing or certificate issuance.
        </p>
      </section>
      <section>
        <h2>Organization review</h2>
        <p>
          An organization application is reviewed by a platform administrator. Submission does not
          guarantee approval. A rejected applicant can read the reason, update the application and
          submit it again. Organization permissions depend on approval, active membership and
          specific authority grants.
        </p>
      </section>
      <section>
        <h2>Content and credentials</h2>
        <p>
          Published learning paths, learning progress and assessment attempts belong to an enrolled
          path version. Practice and mock scores do not represent an official result. Organizer
          content must be their own work or used with permission. Realtime challenges, payments and
          certificates shown as previews are not active services. Learning completion is not an
          issued or state-recognized certificate.
        </p>
      </section>
      <section>
        <h2>Acceptable use</h2>
        <p>
          Do not attempt to access another user's account or organization data, impersonate an
          organization, disrupt the platform or submit misleading review information. Organization
          members must only use capabilities granted to them.
        </p>
      </section>
      <section>
        <h2>Availability and changes</h2>
        <p>
          During development, features may change or be unavailable. Before public launch, the
          project owner must complete the operator identity, contact details, dispute process and
          any service-specific commercial terms, then review this draft.
        </p>
      </section>
    </LegalLayout>
  )
}
