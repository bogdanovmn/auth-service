package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

	private final JwtFactory jwtFactory;
	private final JwtBasedUserDetailsFactory jwtBasedUserDetailsFactory;
	private final AccountRepository accountRepository;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
									HttpServletResponse response,
									FilterChain chain) throws ServletException, IOException
	{
		// Get authorization header and validate
		Optional<String> token = new HttpRequest(request).authToken();
		if (token.isEmpty()) {
			chain.doFilter(request, response);
			return;
		}

		// Get jwt token and validate
		Jws<Claims> parsedToken;
		try {
			parsedToken = jwtFactory.checkSignatureAndReturnClaims(token.get());
		} catch (ExpiredJwtException ex) {
			log.debug("JWT token has expired: {}", ex.getMessage());
			chain.doFilter(request, response);
			return;
		} catch (Exception ex) {
			log.warn(
				"JWT token parsing error: {}, token fingerprint: {}",
				ex.getMessage(), fingerprint(token.get())
			);
			chain.doFilter(request, response);
			return;
		}

		Claims claims = parsedToken.getBody();

		Optional<Account> account = accountByClaims(claims);
		if (account.isEmpty()) {
			log.warn("JWT token doesn't belong to an existing account, the request has been rejected");
			chain.doFilter(request, response);
			return;
		}

		if (!isTokenActual(claims, account.get())) {
			log.warn("JWT token is stale, the password has been changed after the token was issued");
			chain.doFilter(request, response);
			return;
		}

		if (!account.get().getStatus().isAvailable()) {
			log.warn(
				"JWT token belongs to the account '{}' with status {}, the request has been rejected",
				account.get().getEmail(), account.get().getStatus()
			);
			chain.doFilter(request, response);
			return;
		}

		UserDetails userDetails;
		try {
			userDetails = jwtBasedUserDetailsFactory.fromJwtClaims(claims, account.get());
		} catch (Exception ex) {
			// Fail closed: a token we can't turn into a principal is not an authentication
			log.warn(
				"JWT token claims are not usable for authentication: {}, token fingerprint: {}",
				ex.getMessage(), fingerprint(token.get())
			);
			chain.doFilter(request, response);
			return;
		}

		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
			userDetails,
			null,
			userDetails.getAuthorities()
		);

		authentication.setDetails(
			new WebAuthenticationDetailsSource().buildDetails(request)
		);

		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

	private Optional<Account> accountByClaims(Claims claims) {
		String userId = claims.get("userId", String.class);
		if (userId == null) {
			return Optional.empty();
		}
		try {
			return accountRepository.findById(UUID.fromString(userId));
		} catch (IllegalArgumentException ex) {
			log.warn("JWT token has a malformed 'userId' claim");
			return Optional.empty();
		}
	}

	private static boolean isTokenActual(Claims claims, Account account) {
		Date issuedAt = claims.getIssuedAt();
		if (issuedAt == null) {
			return true;
		}
		return account.getPasswordChangedAt() == null
			|| issuedAt.getTime() >= account.getPasswordChangedAt().getTime();
	}

	private static String fingerprint(String token) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(
				token.getBytes(StandardCharsets.UTF_8)
			);
			return HexFormat.of().formatHex(digest, 0, 6);
		} catch (NoSuchAlgorithmException ex) {
			return "unknown";
		}
	}

}
