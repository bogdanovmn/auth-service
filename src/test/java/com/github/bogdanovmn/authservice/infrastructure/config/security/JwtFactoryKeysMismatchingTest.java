package com.github.bogdanovmn.authservice.infrastructure.config.security;

import io.jsonwebtoken.security.SignatureException;
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
		"jwt.private-key-path=classpath:jwt/private.der",
		"jwt.public-key-path=classpath:jwt/public_2.der"
	}
)
@ExtendWith(SpringExtension.class)
class JwtFactoryKeysMismatchingTest {
	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void token() {
		final String token = jwtFactory.createToken(
			Map.of("userName", "Joe")
		);

		assertThrows(
			SignatureException.class,
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
			SignatureException.class,
			() -> jwtFactory.checkSignatureAndReturnClaims(token)
		);
	}
}