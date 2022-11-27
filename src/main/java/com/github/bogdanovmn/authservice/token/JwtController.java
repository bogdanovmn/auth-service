package com.github.bogdanovmn.authservice.token;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/jwt")
@RequiredArgsConstructor
class JwtController {
	private final JwtService jwtService;

	@PostMapping
	JwtResponse exchangeCredentialsToJwt(@RequestBody @Valid ExchangeCredentialsToJwtRequest request) {
		return jwtService.createTokensByAccountCredentials(request);
	}

	@PutMapping
	JwtResponse refreshJwt(@RequestBody @Valid RefreshJwtRequest request) {
		return jwtService.createTokensByRefreshToken(request.getRefreshToken());
	}
}
