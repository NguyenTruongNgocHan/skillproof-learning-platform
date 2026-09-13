package com.skillproof.backend.identity.api;

import com.skillproof.backend.common.exception.ApiError;
import com.skillproof.backend.identity.application.RegisterUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
@Tag(
        name = "Identity",
        description = "Identity, authentication, profile, and account lifecycle APIs."
)
public class IdentityController {

    private final RegisterUserService registerUserService;

    public IdentityController(
            RegisterUserService registerUserService
    ) {
        this.registerUserService = registerUserService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register learner account",
            description = """
                    Creates a new learner account.

                    The created account starts in PENDING_VERIFICATION
                    and cannot be considered fully active until the
                    email verification flow succeeds.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = RegisterResponse.class
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            ),

            @ApiResponse(
                    responseCode = "409",
                    description = "Email already registered.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiError.class
                            )
                    )
            )
    })
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        RegisterResponse response =
                registerUserService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}