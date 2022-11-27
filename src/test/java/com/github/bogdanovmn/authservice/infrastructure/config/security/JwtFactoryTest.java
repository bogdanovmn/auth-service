package com.github.bogdanovmn.authservice.infrastructure.config.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = JwtFactory.class)
@ExtendWith(SpringExtension.class)
class JwtFactoryTest {
	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void createToken() {
		final String token = jwtFactory.createToken(
			Map.of("userName", "Joe")
		);

		assertEquals(
			"Joe",
			jwtFactory.checkSignatureAndReturnClaims(token).getBody().get("userName")
		);
	}

	@Test
	void createRefreshToken() {
		final UUID id = UUID.randomUUID();
		final String userId = "123";
		final String token = jwtFactory.createRefreshToken(id,
			Map.of("userId", userId)
		);

		assertEquals(
			userId,
			jwtFactory.checkSignatureAndReturnClaims(token).getBody().get("userId")
		);

		assertEquals(
			id.toString(),
			jwtFactory.checkSignatureAndReturnClaims(token).getBody().getId()
		);
	}
}