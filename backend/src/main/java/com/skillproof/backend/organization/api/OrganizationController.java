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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    private UUID actor(Authentication auth) {
        return (UUID) auth.getPrincipal();
    }

    public record CreateRequest(@NotBlank
            @Size(max = 200) String legalName, @NotBlank
            @Size(max = 200) String displayName, @Size(max = 500) String website, @NotBlank
            @Size(max = 120) String industry, @NotBlank
            @Size(max = 120) String country, @Size(max = 120) String registrationNumber, @NotBlank
            @Size(max = 150) String contactName, @NotBlank
            @Email String contactEmail, @Size(max = 60) String contactPhone) {

    }

    public record UpdateRequest(@NotBlank
            @Size(max = 200) String displayName, @Size(max = 500) String website, @NotBlank
            @Size(max = 120) String industry, @Size(max = 60) String contactPhone) {

    }

    public record MemberRequest(@NotBlank
            @Email
            @Size(max = 320) String email) {

    }

    public record GrantRequest(@NotNull OrganizationService.Authority authority, boolean active) {

    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record MemberResponse(@JsonProperty("user_id") UUID userId,
            boolean active, String email, List<String> grants) {

        static MemberResponse from(OrganizationMemberView value) {
            return new MemberResponse(value.userId(), value.active(),
                    value.email(), value.grants());
        }
    }

    public record AuthorityResponse(boolean allowed) {

    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)

    public Organization create(Authentication auth, @Valid @RequestBody CreateRequest r) {
        return service.create(actor(auth), new OrganizationService.Create(r.legalName(), r.displayName(), r.website(), r.industry(), r.country(), r.registrationNumber(), r.contactName(), r.contactEmail(), r.contactPhone()));
    }

    @PostMapping("/mine/resubmit")

    public Organization resubmit(Authentication auth, @Valid @RequestBody CreateRequest r) {
        return service.resubmit(actor(auth), new OrganizationService.Create(r.legalName(), r.displayName(), r.website(), r.industry(), r.country(), r.registrationNumber(), r.contactName(), r.contactEmail(), r.contactPhone()));
    }

    @GetMapping("/mine")

    public Organization mine(Authentication auth) {
        return service.mine(actor(auth));
    }

    @GetMapping("/{id}")

    public Organization get(Authentication auth, @PathVariable UUID id) {
        return service.get(id, actor(auth), auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @PatchMapping("/{id}")

    public Organization update(Authentication auth, @PathVariable UUID id, @Valid @RequestBody UpdateRequest r) {
        return service.update(id, actor(auth), new OrganizationService.Update(r.displayName(), r.website(), r.industry(), r.contactPhone()));
    }

    @GetMapping("/{id}/members")

    public List<MemberResponse> members(Authentication auth, @PathVariable UUID id) {
        return service.members(id, actor(auth)).stream().map(MemberResponse::from).toList();
    }

    @PostMapping("/{id}/members")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void add(Authentication auth, @PathVariable UUID id, @Valid @RequestBody MemberRequest r) {
        service.addMember(id, actor(auth), r.email());
    }

    @PatchMapping("/{id}/members/{userId}/grants")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void grant(Authentication auth, @PathVariable UUID id, @PathVariable UUID userId, @Valid @RequestBody GrantRequest r) {
        service.setGrant(id, actor(auth), userId, new OrganizationService.Grant(r.authority(), r.active()));
    }

    @DeleteMapping("/{id}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)

    public void remove(Authentication auth, @PathVariable UUID id, @PathVariable UUID userId) {
        service.removeMember(id, actor(auth), userId);
    }

    @GetMapping("/{id}/authority/{authority}")

    public AuthorityResponse authority(Authentication auth, @PathVariable UUID id,
            @PathVariable OrganizationService.Authority authority) {
        return new AuthorityResponse(service.can(id, actor(auth), authority.name()));
    }
}
