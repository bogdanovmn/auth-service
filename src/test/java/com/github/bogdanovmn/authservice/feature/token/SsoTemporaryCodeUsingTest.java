package com.github.bogdanovmn.authservice.feature.token;

import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
		when(jwtService.createTokensByAccountCredentials(email, password, AccountSecurityEventType.SSO))
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

	@Test
	void temporaryCodeIsConsumedByASingleConcurrentRequest() throws Exception {
		String email = "email@mail.com";
		String password = "pass";
		when(jwtService.createTokensByAccountCredentials(email, password, AccountSecurityEventType.SSO))
			.thenReturn(
				JwtResponse.builder()
					.refreshToken("r-token")
					.token("token")
				.build()
			);

		String code = ssoService.temporaryCode(email, password);

		int threads = 16;
		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			CountDownLatch start = new CountDownLatch(1);
			List<Future<JwtResponse>> results = new ArrayList<>();
			for (int i = 0; i < threads; i++) {
				results.add(
					pool.submit(
						() -> {
							start.await();
							return ssoService.tokens(code);
						}
					)
				);
			}

			start.countDown();

			long granted = 0;
			for (Future<JwtResponse> result : results) {
				try {
					assertEquals("token", result.get(5, TimeUnit.SECONDS).getToken());
					granted++;
				} catch (ExecutionException ex) {
					assertTrue(
						ex.getCause() instanceof NoSuchElementException,
						"Unexpected failure: %s".formatted(ex.getCause())
					);
				}
			}

			assertEquals(1, granted, "A single use code must be exchanged exactly once");
		} finally {
			pool.shutdownNow();
		}
	}
}