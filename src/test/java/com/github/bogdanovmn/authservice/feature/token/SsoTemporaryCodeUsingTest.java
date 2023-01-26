package com.github.bogdanovmn.authservice.feature.token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(
	classes = {
		SsoService.class,
		CacheConfig.class
	}
)
class SsoTemporaryCodeUsingTest {
	@MockBean
	private JwtService jwtService;
	@Autowired
	private SsoService ssoService;

	@Test
	void temporaryCodeOneTimeUsing() {
		String email = "email@mail.com";
		String password = "pass";
		when(jwtService.createTokensByAccountCredentials(email, password))
			.thenReturn(
				JwtResponse.builder()
					.refreshToken("r-token")
					.token("token")
				.build()
			);

		String code = ssoService.temporaryCode(email, password);
		JwtResponse tokens = ssoService.tokens(code);
		assertEquals("token", tokens.getToken());
		assertEquals("r-token", tokens.getRefreshToken());

		assertThrows(
			NoSuchElementException.class,
			() -> ssoService.tokens(code)
		);
	}
}