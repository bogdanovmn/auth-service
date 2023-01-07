package com.github.bogdanovmn.authservice.feature.token;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

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

	@DeleteMapping
	ResponseEntity<?> deleteRefreshToken(Principal currentUser) {
		jwtService.deleteRefreshToken(currentUser.getName());
		return ResponseEntity.ok().build();
	}
}
