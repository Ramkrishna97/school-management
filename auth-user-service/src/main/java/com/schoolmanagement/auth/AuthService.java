package com.schoolmanagement.auth;

import com.schoolmanagement.auth.UserAccount.Role;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserAccountRepository users;
    private final ParentStudentLinkRepository links;
    private final JwtEncoder jwtEncoder;
    private final BCryptPasswordEncoder passwords = new BCryptPasswordEncoder();
    private final String bootstrapPassword;

    public AuthService(UserAccountRepository users, ParentStudentLinkRepository links, JwtEncoder jwtEncoder,
                       @Value("${school.bootstrap.password}") String bootstrapPassword) {
        this.users = users;
        this.links = links;
        this.jwtEncoder = jwtEncoder;
        this.bootstrapPassword = bootstrapPassword;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void hashSeedPasswords() {
        // data.sql uses a marker so the demo password is never stored in plaintext.
        users.findAll().stream().filter(user -> "BOOTSTRAP".equals(user.getPasswordHash()))
            .forEach(user -> user.setPasswordHash(passwords.encode(bootstrapPassword)));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String username, String password) {
        UserAccount user = users.findByUsernameIgnoreCase(username.trim()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!user.isEnabled() || !passwords.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        Instant now = Instant.now();
        JwtClaimsSet.Builder builder = JwtClaimsSet.builder().issuer("school-management")
            .subject(user.getUsername()).issuedAt(now).expiresAt(now.plusSeconds(8 * 3600))
            .claim("roles", List.of(user.getRole().name()))
            .claim("userId", user.getId());
        if (user.getTeacherId() != null) builder.claim("teacherId", user.getTeacherId());
        if (user.getStudentId() != null) builder.claim("studentId", user.getStudentId());
        JwtClaimsSet claims = builder.build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
            JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new LoginResponse(token, user.getUsername(), user.getRole(),
            user.getTeacherId(), 8 * 3600);
    }

    @Transactional(readOnly = true)
    public Profile profile(String username) {
        UserAccount user = users.findByUsernameIgnoreCase(username).orElseThrow();
        List<Long> studentIds = user.getRole() == Role.PARENT
            ? links.findByParentUserId(user.getId()).stream().map(ParentStudentLink::getStudentId).toList()
            : List.of();
        return new Profile(user.getId(), user.getUsername(), user.getRole(),
            user.getTeacherId(), user.getStudentId(), studentIds);
    }

    @Transactional
    public ParentStudentLink linkParent(Long parentUserId, Long studentId) {
        UserAccount parent = users.findById(parentUserId).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent account not found"));
        if (parent.getRole() != Role.PARENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not a parent");
        }
        if (links.existsByParentUserIdAndStudentId(parentUserId, studentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student already linked to parent");
        }
        return links.save(new ParentStudentLink(parentUserId, studentId));
    }

    public record LoginResponse(String accessToken, String username, Role role,
                                Long teacherId, int expiresInSeconds) {}
    public record Profile(Long id, String username, Role role, Long teacherId,
                          Long studentId, List<Long> linkedStudentIds) {}
}
