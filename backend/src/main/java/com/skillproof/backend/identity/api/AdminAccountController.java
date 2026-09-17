package com.skillproof.backend.identity.api;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.skillproof.backend.identity.application.AdminAccountService;
import com.skillproof.backend.identity.application.RequestMetadata;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/accounts")
@SecurityRequirement(name = "bearerAuth")
public class AdminAccountController {

    private final AdminAccountService accounts;

    public AdminAccountController(AdminAccountService accounts) {
        this.accounts = accounts;
    }

    @GetMapping
    public Page<AdminAccountResponse> list(Pageable pageable) {
        return accounts.list(pageable);
    }

    @PatchMapping("/{accountId}/status")
    public AdminAccountResponse changeStatus(
            Authentication authentication,
            @PathVariable UUID accountId,
            @Valid @RequestBody ChangeAccountStatusRequest request,
            HttpServletRequest servletRequest
    ) {
        return accounts.changeStatus(
                (UUID) authentication.getPrincipal(),
                accountId,
                request.status(),
                metadata(servletRequest)
        );
    }

    @PatchMapping("/{accountId}/role")
    public AdminAccountResponse changeRole(
            Authentication authentication,
            @PathVariable UUID accountId,
            @Valid @RequestBody ChangeAccountRoleRequest request,
            HttpServletRequest servletRequest
    ) {
        return accounts.changeRole(
                (UUID) authentication.getPrincipal(),
                accountId,
                request.role(),
                metadata(servletRequest)
        );
    }

    private RequestMetadata metadata(HttpServletRequest request) {
        return new RequestMetadata(
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );
    }
}
