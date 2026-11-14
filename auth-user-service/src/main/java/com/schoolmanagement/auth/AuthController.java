package com.schoolmanagement.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record LinkStudent(@NotNull Long studentId) {}

    @PostMapping("/login")
    public AuthService.LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request.username(), request.password());
    }

    @GetMapping("/me")
    public AuthService.Profile me(@AuthenticationPrincipal Jwt jwt) {
        return service.profile(jwt.getSubject());
    }

    @PostMapping("/parents/{parentId}/students")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ParentStudentLink link(@PathVariable Long parentId,
                                  @Valid @RequestBody LinkStudent request) {
        return service.linkParent(parentId, request.studentId());
    }
}
