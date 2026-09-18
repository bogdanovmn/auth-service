package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import io.jsonwebtoken.Claims;
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
import java.util.Date;
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
		} catch (Exception ex) {
			log.warn("JWT token parsing error: {}, token: {}", ex.getMessage(), token);
			chain.doFilter(request, response);
			return;
		}

		if (!isTokenActual(parsedToken.getBody())) {
			log.warn("JWT token is stale, the password has been changed after the token was issued");
			chain.doFilter(request, response);
			return;
		}

		UserDetails userDetails = jwtBasedUserDetailsFactory.fromJwtClaims(parsedToken.getBody());
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

	private boolean isTokenActual(Claims claims) {
		String userId = claims.get("userId", String.class);
		if (userId == null) {
			// A token without a user id can't be linked to a password change
			return true;
		}
		Date issuedAt = claims.getIssuedAt();
		if (issuedAt == null) {
			return true;
		}
		return accountRepository.findById(UUID.fromString(userId))
			.map(
				account -> account.getPasswordChangedAt() == null
					|| issuedAt.getTime() >= account.getPasswordChangedAt().getTime()
			)
			.orElse(false);
	}

}
