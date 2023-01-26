package com.github.bogdanovmn.authservice.feature.token;

import com.github.benmanes.caffeine.cache.Cache;
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
			jwtService.createTokensByAccountCredentials(email, password)
		);
	}

	public JwtResponse tokens(String code) {
		JwtResponse tokens = temporaryCodeCache.getIfPresent(code);
		if (tokens == null) {
			throw new NoSuchElementException("There are no tokens for the code");
		}
		temporaryCodeCache.invalidate(code);
		return tokens;
	}

	public String temporaryCodeByAccount(String accountName) {
		return cachedCode(
			jwtService.createTokensByAccountName(accountName)
		);
	}

	private String cachedCode(JwtResponse tokens) {
		String code = "%s-%s".formatted(System.currentTimeMillis(), UUID.randomUUID().toString());
		temporaryCodeCache.put(code, tokens);
		return code;
	}
}
