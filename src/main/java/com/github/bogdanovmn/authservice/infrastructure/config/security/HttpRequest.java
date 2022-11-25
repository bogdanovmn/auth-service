package com.github.bogdanovmn.authservice.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RequiredArgsConstructor
class HttpRequest {
	private final HttpServletRequest request;

	public Optional<String> authToken() {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		return (header != null && header.startsWith("Bearer "))
			? Optional.of(
				header.split(" ")[1].trim()
			)
			: Optional.empty();
	}
}
