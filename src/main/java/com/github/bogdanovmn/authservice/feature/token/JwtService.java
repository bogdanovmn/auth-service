package com.github.bogdanovmn.authservice.feature.token;

import com.github.bogdanovmn.authservice.common.domain.AccountService;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtFactory;
import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.Role;
import com.github.bogdanovmn.authservice.infrastructure.audit.SecurityEventLogger;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
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
public class JwtService {
	private static final PasswordEncoder PASSWORD_ENCODER =
		PasswordEncoderFactories.createDelegatingPasswordEncoder();

	private final AccountService accountService;
	private final JwtFactory jwtFactory;
	private final RefreshTokenRepository refreshTokenRepository;
	private final SecurityEventLogger securityEventLogger;

	@Transactional
	public JwtResponse createTokensByAccountCredentials(String email, String password, AccountSecurityEventType successEventType) {
		Optional<Account> candidate = accountService.getByEmail(email);
		if (candidate.isEmpty()) {
			securityEventLogger.logUnknownAttempt(email);
			throw new NoSuchElementException("Can't find a user with the email and password");
		}
		Account account = candidate.get();
		if (!PASSWORD_ENCODER.matches(password, account.getEncodedPassword())) {
			securityEventLogger.log(account.getId(), AccountSecurityEventType.LOGIN_FAILED);
			throw new NoSuchElementException("Can't find a user with the email and password");
		}
		securityEventLogger.log(account.getId(), successEventType);
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
		securityEventLogger.log(account.getId(), AccountSecurityEventType.REFRESH);
		return responseWithTokens(account);
	}

	@Transactional
	public JwtResponse createTokensByAccountName(String accountName) {
		Account account = accountService.getByName(accountName);
		securityEventLogger.log(account.getId(), AccountSecurityEventType.SSO);
		return responseWithTokens(account);
	}

	@Transactional
	public void logout(String userName) {
		Account account = accountService.getByName(userName);
		deleteRefreshToken(account);
		securityEventLogger.log(account.getId(), AccountSecurityEventType.LOGOUT);
	}

	@Transactional
	public void deleteRefreshToken(String userName) {
		deleteRefreshToken(accountService.getByName(userName));
	}

	private void deleteRefreshToken(Account account) {
		log.info("Refresh JWT token deleting for {}", account);
		Optional<RefreshToken> previousRefreshToken = refreshTokenRepository.getByAccount(account);
		previousRefreshToken.ifPresent(
			rt -> {
				refreshTokenRepository.delete(rt);
				refreshTokenRepository.flush();
				log.info("Previous refresh token has been deleted: {}", rt);
			}
		);
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
		deleteRefreshToken(account);
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
