package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.domain.Account;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtBasedUserDetailsFactoryTest {

	private static final String EMAIL = "joe@mail.ru";
	private static final String NAME = "Joe";

	private final JwtBasedUserDetailsFactory factory = new JwtBasedUserDetailsFactory(
		securityRolesProperties()
	);

	private final Account account = new Account()
		.setId(UUID.randomUUID())
		.setName(NAME)
		.setEmail(EMAIL);

	@Test
	void principalIsTheAccountEmailNotTheUserNameClaim() {
		UserDetails userDetails = factory.fromJwtClaims(
			accountClaims(Map.of("userName", NAME, "roles", List.of("any:user"))),
			account
		);

		assertEquals(EMAIL, userDetails.getUsername());
	}

	@Test
	void emailPrincipalIsUsedEvenWhenTheNameClaimIsDuplicated() {
		UserDetails userDetails = factory.fromJwtClaims(
			accountClaims(Map.of("userName", "joe", "roles", List.of("any:user"))),
			account
		);

		assertEquals(EMAIL, userDetails.getUsername());
	}

	@Test
	void tokenWithoutUserIdClaimIsRejected() {
		Claims claims = claims(Map.of("userName", NAME, "roles", List.of("any:admin")));

		assertThrows(
			IllegalArgumentException.class,
			() -> factory.fromJwtClaims(claims, account)
		);
	}

	@Test
	void tokenWithBlankUserIdClaimIsRejected() {
		Claims claims = claims(Map.of("userId", "  ", "userName", NAME, "roles", List.of("any:admin")));

		assertThrows(
			IllegalArgumentException.class,
			() -> factory.fromJwtClaims(claims, account)
		);
	}

	@Test
	void applicationPrefixIsStrippedFromRoles() {
		UserDetails userDetails = factory.fromJwtClaims(
			accountClaims(Map.of("userName", NAME, "roles", List.of("auth:moderator", "any:admin", "other:user"))),
			account
		);

		assertEquals(
			List.of("moderator", "admin", "other:user"),
			userDetails.getAuthorities().stream().map(Object::toString).toList()
		);
	}

	@Test
	void missingRolesClaimDoesNotPreventAuthentication() {
		UserDetails userDetails = factory.fromJwtClaims(
			accountClaims(Map.of("userName", NAME)),
			account
		);

		assertEquals(EMAIL, userDetails.getUsername());
		assertEquals(List.of(), List.copyOf(userDetails.getAuthorities()));
	}

	private static SecurityRolesProperties securityRolesProperties() {
		SecurityRolesProperties properties = new SecurityRolesProperties();
		properties.setApplicationIdPrefix("auth");
		return properties;
	}

	private Claims accountClaims(Map<String, Object> values) {
		Map<String, Object> all = new HashMap<>(values);
		all.put("userId", account.getId().toString());
		return new DefaultClaims(all);
	}

	private static Claims claims(Map<String, Object> values) {
		return new DefaultClaims(values);
	}
}
