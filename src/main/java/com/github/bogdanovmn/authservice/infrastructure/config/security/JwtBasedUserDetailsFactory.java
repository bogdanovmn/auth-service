package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.domain.Application;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableConfigurationProperties(SecurityRolesProperties.class)
@RequiredArgsConstructor
public class JwtBasedUserDetailsFactory {

    private final SecurityRolesProperties properties;

    public UserDetails fromJwtClaims(Claims claims) {
        List<? extends GrantedAuthority> roles = ((List<String>) claims.get("roles")).stream()
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

        return new JwtBasedUserDetails(
            claims.get("userName", String.class),
            roles
        );
    }
}
