package com.github.bogdanovmn.authservice;

import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.RefreshTokenRepository;
import com.github.bogdanovmn.authservice.test.fixture.RoleFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = JwtService.class)
@ExtendWith(SpringExtension.class)
class JwtServiceTest {

	@Autowired
	private JwtService jwtService;

	@MockBean
	private RefreshTokenRepository refreshTokenRepository;

	@Test
	void createToken() {
		String token = jwtService.createToken(
			new Account()
				.setId(UUID.randomUUID())
				.setName("Joe")
				.setRoles(
					Set.of(
						RoleFixture.standardUser()
					)
				)
		);

		assertEquals(
			"Joe",
			jwtService.parse(token).getBody().get("userName")
		);
	}
}