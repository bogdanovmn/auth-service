package com.github.bogdanovmn.authservice.infrastructure.config.security;

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
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenFilter extends OncePerRequestFilter {

	private final JwtFactory jwtFactory;
	private final JwtBasedUserDetailsFactory jwtBasedUserDetailsFactory;

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

}
