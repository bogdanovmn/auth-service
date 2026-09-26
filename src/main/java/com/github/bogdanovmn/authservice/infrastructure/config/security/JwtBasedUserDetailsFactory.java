package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.Application;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Component
@EnableConfigurationProperties(SecurityRolesProperties.class)
@RequiredArgsConstructor
public class JwtBasedUserDetailsFactory {

    private final SecurityRolesProperties properties;

    /**
     * The principal name is the account email, the only unique account identifier.
     * The {@code userName} claim carries the display name and is not used to
     * identify the account, but its presence marks the token as an access token.
     *
     * @param claims  access token claims
     * @param account the account the token has been issued for
     */
    public UserDetails fromJwtClaims(Claims claims, Account account) {
        String userId = claims.get("userId", String.class);
        if (userId == null || userId.isBlank()) {
            // A token without a principal (e.g. a refresh token passed as a bearer one)
            // must not be treated as an access token
            throw new IllegalArgumentException(
                "The token has no 'userId' claim and can't be used for authentication"
            );
        }

        List<? extends GrantedAuthority> roles = claimValues(claims.get("roles"))
            .map(String::valueOf)
            .map(
                r -> r.replaceFirst(
                    "^(%s|%s):".formatted(
                        Application.ANY_APPLICATION, properties.getApplicationIdPrefix()
                    ),
                    ""
                )
            )
            .map(SimpleGrantedAuthority::new)
            .toList();

        return new JwtBasedUserDetails(account.getEmail(), roles);
    }

    private static Stream<String> claimValues(Object claim) {
        if (claim instanceof Collection<?> values) {
            return values.stream().map(String::valueOf);
        }
        return claim == null ? Stream.empty() : Stream.of(String.valueOf(claim));
    }
}
