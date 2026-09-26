package com.github.bogdanovmn.authservice.feature.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
class SsoService {
	private final JwtService jwtService;
	private final Cache<String, JwtResponse> temporaryCodeCache;

	public String temporaryCode(String email, String password) {
		return cachedCode(
			jwtService.createTokensByAccountCredentials(email, password, AccountSecurityEventType.SSO)
		);
	}

	public JwtResponse tokens(String code) {
		// getIfPresent() + invalidate() would let two concurrent requests read the
		// same code before either invalidates it. Removing from the map is atomic,
		// so a code is consumed by exactly one caller.
		JwtResponse tokens = temporaryCodeCache.asMap().remove(code);
		if (tokens == null) {
			throw new NoSuchElementException("There are no tokens for the code");
		}
		return tokens;
	}

	public String temporaryCodeByAccount(String email) {
		return cachedCode(
			jwtService.createTokensByAccountEmail(email)
		);
	}

	private String cachedCode(JwtResponse tokens) {
		String code = "%s-%s".formatted(System.currentTimeMillis(), UUID.randomUUID().toString());
		temporaryCodeCache.put(code, tokens);
		return code;
	}
}
