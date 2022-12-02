package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.model.Application;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtBasedUserDetailsFactory {

    @Value("${security.roles.application-id-prefix}")
    private final String roleApplicationPrefix;

    public UserDetails fromJwtClaims(Claims claims) {
        List<? extends GrantedAuthority> roles = ((List<String>) claims.get("roles")).stream()
            .map(
                r -> r.replaceFirst(
                    "^(%s|%s):".formatted(
                        Application.ANY_APPLICATION, roleApplicationPrefix
                    ),
                    ""
                )
            )
            .map(SimpleGrantedAuthority::new)
            .toList();

        return new JwtBasedUserDetails(
            claims.get("userName", String.class),
            roles
        );
    }
}
