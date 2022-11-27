package com.github.bogdanovmn.authservice.infrastructure.config.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
	classes = JwtFactory.class,
	properties = {
		"jwt.ttl-in-minutes:-10",
		"jwt.refresh-token.ttl-in-hours:-48"
	}
)
@ExtendWith(SpringExtension.class)
class JwtExpirationTest {

	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void token() {
		final String token = jwtFactory.createToken(
			Map.of("userName", "Joe")
		);

		assertThrows(
			ExpiredJwtException.class,
			() -> jwtFactory.checkSignatureAndReturnClaims(token)
		);
	}

	@Test
	void refreshToken() {
		final String token = jwtFactory.createRefreshToken(
			UUID.randomUUID(),
			Map.of("userId", "123")
		);

		assertThrows(
			ExpiredJwtException.class,
			() -> jwtFactory.checkSignatureAndReturnClaims(token)
		);
	}
}