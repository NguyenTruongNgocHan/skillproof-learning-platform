package com.skillproof.backend.organization.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillproof.backend.common.exception.BadRequestException;
import com.skillproof.backend.common.exception.ConflictException;
import com.skillproof.backend.common.exception.NotFoundException;
import com.skillproof.backend.identity.contract.IdentityAccessQuery;
import com.skillproof.backend.organization.infrastructure.OrganizationInvitationEntity;
import com.skillproof.backend.organization.infrastructure.OrganizationInvitationJpaRepository;
import com.skillproof.backend.organization.infrastructure.OrganizationRepository;

@Service
public class OrganizationInvitationService {

    private static final Duration TTL = Duration.ofDays(7);
    private final OrganizationInvitationJpaRepository invitations;
    private final OrganizationRepository organizations;
    private final IdentityAccessQuery users;
    private final JavaMailSender mail;
    private final SecureRandom random = new SecureRandom();

    public OrganizationInvitationService(
        OrganizationInvitationJpaRepository invitations,
        OrganizationRepository organizations,
        IdentityAccessQuery users,
        JavaMailSender mail
    ) {
        this.invitations = invitations;
        this.organizations = organizations;
        this.users = users;
        this.mail = mail;
    }

    public record InvitationView(
        UUID id,
        UUID organizationId,
        String email,
        String status,
        Instant expiresAt,
        Instant createdAt
    ) {}

    @Transactional
    public InvitationView invite(
        UUID actor,
        UUID organizationId,
        String email
    ) {
        if (
            !users.isActiveOrganizer(actor) ||
            !organizations.hasGrant(organizationId, actor, "MANAGE_MEMBERS")
        ) throw new org.springframework.security.access.AccessDeniedException(
            "Organization authority required"
        );
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        var account = users
            .findByEmail(normalized)
            .orElseThrow(() ->
                new NotFoundException("USER_NOT_FOUND", "Account not found")
            );
        if (
            !account.active() || !"ORGANIZER".equals(account.role())
        ) throw new BadRequestException(
            "MEMBER_NOT_ELIGIBLE",
            "Active organizer required"
        );
        if (
            organizations.activeMember(organizationId, account.id())
        ) throw new ConflictException(
            "MEMBER_EXISTS",
            "Member already belongs to this organization"
        );
        invitations
            .findFirstByOrganizationIdAndEmailAndStatusOrderByCreatedAtDesc(
                organizationId,
                normalized,
                "PENDING"
            )
            .ifPresent(existing -> {
                if (
                    existing.getExpiresAt().isAfter(Instant.now())
                ) throw new ConflictException(
                    "INVITATION_PENDING",
                    "An invitation is already pending"
                );
                existing.expire();
                invitations.saveAndFlush(existing);
            });
        String raw = randomToken();
        Instant now = Instant.now();
        var entity = invitations.saveAndFlush(
            new OrganizationInvitationEntity(
                UUID.randomUUID(),
                organizationId,
                normalized,
                hash(raw),
                actor,
                now.plus(TTL),
                now
            )
        );
        send(normalized, raw, entity.getExpiresAt());
        return view(entity);
    }

    @Transactional(readOnly = true)
    public List<InvitationView> list(UUID actor, UUID organizationId) {
        if (
            !users.isActiveOrganizer(actor) ||
            !organizations.hasGrant(organizationId, actor, "MANAGE_MEMBERS")
        ) throw new org.springframework.security.access.AccessDeniedException(
            "Organization authority required"
        );
        return invitations
            .findByOrganizationIdOrderByCreatedAtDesc(organizationId)
            .stream()
            .map(this::view)
            .toList();
    }

    @Transactional
    public void revoke(UUID actor, UUID invitationId) {
        var invitation = invitations
            .findForUpdate(invitationId)
            .orElseThrow(() ->
                new NotFoundException(
                    "INVITATION_NOT_FOUND",
                    "Invitation not found"
                )
            );
        if (
            !users.isActiveOrganizer(actor) ||
            !organizations.hasGrant(
                invitation.getOrganizationId(),
                actor,
                "MANAGE_MEMBERS"
            )
        ) throw new org.springframework.security.access.AccessDeniedException(
            "Organization authority required"
        );
        if (
            !"PENDING".equals(invitation.getStatus())
        ) throw new ConflictException(
            "INVITATION_NOT_PENDING",
            "Invitation is not pending"
        );
        invitation.revoke();
    }

    @Transactional
    public InvitationView resend(UUID actor, UUID invitationId) {
        var previous = invitations
            .findForUpdate(invitationId)
            .orElseThrow(() ->
                new NotFoundException(
                    "INVITATION_NOT_FOUND",
                    "Invitation not found"
                )
            );
        if (
            !users.isActiveOrganizer(actor) ||
            !organizations.hasGrant(
                previous.getOrganizationId(),
                actor,
                "MANAGE_MEMBERS"
            )
        ) throw new org.springframework.security.access.AccessDeniedException(
            "Organization authority required"
        );
        if (
            "ACCEPTED".equals(previous.getStatus())
        ) throw new ConflictException(
            "INVITATION_ALREADY_ACCEPTED",
            "Invitation is already accepted"
        );
        var recipient = users
            .findByEmail(previous.getEmail())
            .orElseThrow(() ->
                new NotFoundException(
                    "USER_NOT_FOUND",
                    "Recipient account not found"
                )
            );
        if (
            !recipient.active() || !"ORGANIZER".equals(recipient.role())
        ) throw new BadRequestException(
            "MEMBER_NOT_ELIGIBLE",
            "Active organizer required"
        );
        if (
            organizations.activeMember(
                previous.getOrganizationId(),
                recipient.id()
            )
        ) throw new ConflictException(
            "MEMBER_EXISTS",
            "Recipient is already a member"
        );
        previous.expire();
        invitations.flush();
        String raw = randomToken();
        Instant now = Instant.now();
        var replacement = invitations.saveAndFlush(
            new OrganizationInvitationEntity(
                UUID.randomUUID(),
                previous.getOrganizationId(),
                previous.getEmail(),
                hash(raw),
                actor,
                now.plus(TTL),
                now
            )
        );
        send(replacement.getEmail(), raw, replacement.getExpiresAt());
        return view(replacement);
    }

    @Transactional
    public void accept(UUID actor, String rawToken) {
        var invitation = invitations
            .findByTokenHashForUpdate(hash(rawToken))
            .orElseThrow(() ->
                new NotFoundException(
                    "INVITATION_NOT_FOUND",
                    "Invitation is invalid or expired"
                )
            );
        var account = users
            .find(actor)
            .filter(value -> value.active() && "ORGANIZER".equals(value.role()))
            .orElseThrow(() ->
                new BadRequestException(
                    "MEMBER_NOT_ELIGIBLE",
                    "Active organizer required"
                )
            );
        if (!account.email().equalsIgnoreCase(invitation.getEmail())) {
            throw new org.springframework.security.access.AccessDeniedException(
                "Invitation email does not match account"
            );
        }
        if ("ACCEPTED".equals(invitation.getStatus())) return;
        if (
            !"PENDING".equals(invitation.getStatus())
        ) throw new ConflictException(
            "INVITATION_NOT_PENDING",
            "Invitation is no longer available"
        );
        if (
            !invitation.getExpiresAt().isAfter(Instant.now())
        ) throw new ConflictException(
            "INVITATION_EXPIRED",
            "Invitation has expired"
        );
        organizations.addMember(
            invitation.getOrganizationId(),
            actor,
            actor,
            Instant.now(),
            false
        );
        invitation.accept(actor, Instant.now());
    }

    public record InvitationPreview(
        UUID organizationId,
        String organizationName,
        String status,
        Instant expiresAt
    ) {}

    @Transactional(readOnly = true)
    public InvitationPreview preview(UUID actor, String rawToken) {
        var value = invitations
            .findByTokenHash(hash(rawToken))
            .orElseThrow(() ->
                new NotFoundException(
                    "INVITATION_NOT_FOUND",
                    "Invitation not found"
                )
            );
        var account = users.find(actor).orElseThrow();
        if (
            !account.email().equalsIgnoreCase(value.getEmail())
        ) throw new org.springframework.security.access.AccessDeniedException(
            "Invitation email does not match account"
        );
        var org = organizations.find(value.getOrganizationId()).orElseThrow();
        return new InvitationPreview(
            org.id(),
            org.displayName(),
            view(value).status(),
            value.getExpiresAt()
        );
    }

    private InvitationView view(OrganizationInvitationEntity value) {
        String status =
            "PENDING".equals(value.getStatus()) &&
            !value.getExpiresAt().isAfter(Instant.now())
                ? "EXPIRED"
                : value.getStatus();
        return new InvitationView(
            value.getId(),
            value.getOrganizationId(),
            value.getEmail(),
            status,
            value.getExpiresAt(),
            value.getCreatedAt()
        );
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String raw) {
        try {
            return HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256").digest(
                    raw.getBytes(StandardCharsets.UTF_8)
                )
            );
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash invitation token", e);
        }
    }

    private void send(String email, String token, Instant expiry) {
        var message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("You have been invited to SkillProof");
        String frontend = System.getenv().getOrDefault(
            "FRONTEND_URL",
            "http://localhost:5173"
        );
        message.setText(
            "You have been invited to join an organization. Accept the invitation before " +
                expiry +
                ":\n\n" +
                frontend +
                "/organizer/invitations/" +
                token +
                "/accept"
        );
        mail.send(message);
    }
}
