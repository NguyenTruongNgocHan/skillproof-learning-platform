package com.skillproof.backend.organization.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skillproof.backend.organization.application.OrganizationInvitationService;
import com.skillproof.backend.organization.application.OrganizationService;
import com.skillproof.backend.organization.domain.Organization;
import com.skillproof.backend.organization.domain.OrganizationMemberView;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/v1/organizations")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationService service;
    private final OrganizationInvitationService invitations;

    public OrganizationController(
            OrganizationService service,
            OrganizationInvitationService invitations
    ) {
        this.service = service;
        this.invitations = invitations;
    }

    private UUID actor(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    public record CreateRequest(
            @NotBlank
            @Size(max = 200) String legalName,
            @NotBlank
            @Size(max = 200) String displayName,
            @Size(max = 500)
            @jakarta.validation.constraints.Pattern(
                    regexp = "(?i)^(https?://[^\\s]+)?$"
            )
            String website,
            @NotBlank
            @Size(max = 120) String industry,
            @NotBlank
            @Size(max = 120) String country,
            @Size(max = 120) String registrationNumber,
            @NotBlank
            @Size(max = 150) String contactName,
            @NotBlank
            @Email
            @Size(max = 320) String contactEmail,
            @Size(max = 60) String contactPhone
            ) {

    }

    public record UpdateRequest(
            @NotBlank
            @Size(max = 200) String displayName,
            @Size(max = 500)
            @jakarta.validation.constraints.Pattern(
                    regexp = "(?i)^(https?://[^\\s]+)?$"
            )
            String website,
            @NotBlank
            @Size(max = 120) String industry,
            @Size(max = 60) String contactPhone
            ) {

    }

    public record MemberRequest(
            @NotBlank
            @Email
            @Size(max = 320) String email
            ) {

    }

    public record GrantRequest(
            @NotNull OrganizationService.Authority authority,
            boolean active
            ) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MemberResponse(
            @JsonProperty("user_id") UUID userId,
            boolean active,
            String email,
            List<String> grants
            ) {

        static MemberResponse from(OrganizationMemberView value) {
            return new MemberResponse(
                    value.userId(),
                    value.active(),
                    value.email(),
                    value.grants()
            );
        }
    }

    public record AuthorityResponse(boolean allowed) {

    }

    public record InvitationRequest(
            @NotBlank
            @Email
            @Size(max = 320) String email
            ) {

    }

    @GetMapping("/{id}/invitations")
    public List<OrganizationInvitationService.InvitationView> invitations(
            Authentication auth,
            @PathVariable UUID id
    ) {
        return invitations.list(actor(auth), id);
    }

    @PostMapping("/{id}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public OrganizationInvitationService.InvitationView invite(
            Authentication auth,
            @PathVariable UUID id,
            @Valid @RequestBody InvitationRequest request
    ) {
        return invitations.invite(actor(auth), id, request.email());
    }

    @DeleteMapping("/invitations/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revokeInvitation(
            Authentication auth,
            @PathVariable UUID invitationId
    ) {
        invitations.revoke(actor(auth), invitationId);
    }

    @PostMapping("/invitations/{invitationId}/resend")
    public OrganizationInvitationService.InvitationView resendInvitation(
            Authentication auth,
            @PathVariable UUID invitationId
    ) {
        return invitations.resend(actor(auth), invitationId);
    }

    @GetMapping("/invitations/{token}")
    public OrganizationInvitationService.InvitationPreview previewInvitation(
            Authentication auth,
            @PathVariable String token
    ) {
        return invitations.preview(actor(auth), token);
    }

    @PostMapping("/invitations/{token}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvitation(
            Authentication auth,
            @PathVariable String token
    ) {
        invitations.accept(actor(auth), token);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Organization create(
            Authentication auth,
            @Valid @RequestBody CreateRequest r
    ) {
        return service.create(
                actor(auth),
                new OrganizationService.Create(
                        r.legalName(),
                        r.displayName(),
                        r.website(),
                        r.industry(),
                        r.country(),
                        r.registrationNumber(),
                        r.contactName(),
                        r.contactEmail(),
                        r.contactPhone()
                )
        );
    }

    @PostMapping("/mine/resubmit")
    public Organization resubmit(
            Authentication auth,
            @Valid @RequestBody CreateRequest r
    ) {
        return service.resubmit(
                actor(auth),
                new OrganizationService.Create(
                        r.legalName(),
                        r.displayName(),
                        r.website(),
                        r.industry(),
                        r.country(),
                        r.registrationNumber(),
                        r.contactName(),
                        r.contactEmail(),
                        r.contactPhone()
                )
        );
    }

    @PutMapping("/mine/draft")
    public Organization saveDraft(
            Authentication auth,
            @Valid @RequestBody CreateRequest r
    ) {
        return service.saveDraft(
                actor(auth),
                new OrganizationService.Create(
                        r.legalName(),
                        r.displayName(),
                        r.website(),
                        r.industry(),
                        r.country(),
                        r.registrationNumber(),
                        r.contactName(),
                        r.contactEmail(),
                        r.contactPhone()
                )
        );
    }

    @PostMapping("/mine/submit")
    public Organization submit(Authentication auth) {
        return service.submit(actor(auth));
    }

    @GetMapping("/mine")
    public Organization mine(Authentication auth) {
        return service.mine(actor(auth));
    }

    @GetMapping("/{id}/application-revisions")
    public List<com.skillproof.backend.organization.domain.ApplicationRevisionView> applicationRevisions(
            Authentication auth,
            @PathVariable UUID id
    ) {
        return service.applicationRevisions(actor(auth), id);
    }

    @GetMapping("/mine/all")
    public List<Organization> memberships(Authentication auth) {
        return service.memberships(actor(auth));
    }

    @GetMapping("/{id}")
    public Organization get(Authentication auth, @PathVariable UUID id) {
        return service.get(
                id,
                actor(auth),
                auth
                        .getAuthorities()
                        .stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))
        );
    }

    @PatchMapping("/{id}")
    public Organization update(
            Authentication auth,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRequest r
    ) {
        return service.update(
                id,
                actor(auth),
                new OrganizationService.Update(
                        r.displayName(),
                        r.website(),
                        r.industry(),
                        r.contactPhone()
                )
        );
    }

    @GetMapping("/{id}/members")
    public List<MemberResponse> members(
            Authentication auth,
            @PathVariable UUID id
    ) {
        return service
                .members(id, actor(auth))
                .stream()
                .map(MemberResponse::from)
                .toList();
    }

    @PatchMapping("/{id}/members/{userId}/grants")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void grant(
            Authentication auth,
            @PathVariable UUID id,
            @PathVariable UUID userId,
            @Valid @RequestBody GrantRequest r
    ) {
        service.setGrant(
                id,
                actor(auth),
                userId,
                new OrganizationService.Grant(r.authority(), r.active())
        );
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(
            Authentication auth,
            @PathVariable UUID id,
            @PathVariable UUID userId
    ) {
        service.removeMember(id, actor(auth), userId);
    }

    @GetMapping("/{id}/authority/{authority}")
    public AuthorityResponse authority(
            Authentication auth,
            @PathVariable UUID id,
            @PathVariable OrganizationService.Authority authority
    ) {
        return new AuthorityResponse(
                service.can(id, actor(auth), authority.name())
        );
    }
}
