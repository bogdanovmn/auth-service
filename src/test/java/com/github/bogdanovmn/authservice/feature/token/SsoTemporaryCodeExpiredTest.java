package com.github.bogdanovmn.authservice.feature.token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@SpringBootTest(
	classes = {
		SsoService.class,
		CacheConfig.class
	},
	properties = {
		"cache.temporary-code.ttl-in-sec=0"
	}
)
class SsoTemporaryCodeExpiredTest {
	@MockBean
	private JwtService jwtService;
	@Autowired
	private SsoService ssoService;

	@Test
	void temporaryCodeExpired() {
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
		assertThrows(
			NoSuchElementException.class,
			() -> ssoService.tokens(code)
		);
	}
}