package com.github.bogdanovmn.authservice.token;

import com.github.bogdanovmn.authservice.AccountService;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtFactory;
import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.RefreshToken;
import com.github.bogdanovmn.authservice.model.RefreshTokenRepository;
import com.github.bogdanovmn.authservice.model.Role;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
class JwtService {
	private final AccountService accountService;
	private final JwtFactory jwtFactory;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public JwtResponse createTokensByAccountCredentials(ExchangeCredentialsToJwtRequest credentials) {
		Account account = accountService.getByEmailAndPassword(credentials.getEmail(), credentials.getPassword())
			.orElseThrow(() -> new NoSuchElementException("Can't find a user with the email and password"));

		return responseWithTokens(account);
	}

	@Transactional
	public JwtResponse createTokensByRefreshToken(String refreshToken) {
		Claims token = jwtFactory.checkSignatureAndReturnClaims(refreshToken).getBody();
		Optional<RefreshToken> currentToken = refreshTokenRepository.findById(
			UUID.fromString(token.getId())
		);
		if (currentToken.isEmpty()) {
			throw new IllegalArgumentException(
				"Unknown refresh token: %s".formatted(token.getId())
			);
		}
		Account account = currentToken.get().getAccount();

		return responseWithTokens(account);
	}

	private JwtResponse responseWithTokens(Account account) {
		return JwtResponse.builder()
			.token(
				createToken(account)
			)
			.refreshToken(
				createRefreshToken(account)
			)
		.build();
	}

	private String createToken(Account account) {
		log.info("Creating JWT for {}", account);
		return jwtFactory.createToken(
			Map.of(
				"roles",    account.getRoles().stream().map(Role::toString).collect(Collectors.toSet()),
				"userId",   account.getId(),
				"userName", account.getName()
			)
		);
	}

	private String createRefreshToken(Account account) {
		log.info("Creating refresh JWT token for {}", account);
		Optional<RefreshToken> previousRefreshToken = refreshTokenRepository.getByAccount(account);
		previousRefreshToken.ifPresent(
			rt -> {
				refreshTokenRepository.delete(rt);
				refreshTokenRepository.flush();
				log.info("Previous refresh token has been deleted: {}", rt);
			}
		);

		RefreshToken refreshToken = refreshTokenRepository.save(
			new RefreshToken()
				.setAccount(account)
				.setExpiresAt(
					jwtFactory.refreshTokenExpiresAt()
				)
		);

		return jwtFactory.createRefreshToken(
			refreshToken.getId(),
			Map.of("userId", account.getId())
		);

	}
}
