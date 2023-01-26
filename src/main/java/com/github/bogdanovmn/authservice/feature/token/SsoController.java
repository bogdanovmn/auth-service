package com.github.bogdanovmn.authservice.feature.token;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/sso")
@RequiredArgsConstructor
class SsoController {
	private final SsoService ssoService;

	@PostMapping("/code")
	TemporaryCodeResponse exchangeCredentialsToCode(@RequestBody @Valid ExchangeCredentialsToJwtRequest request) {
		return TemporaryCodeResponse.builder()
			.code(
				ssoService.temporaryCode(request.getEmail(), request.getPassword())
			)
		.build();
	}

	@GetMapping("/jwt")
	JwtResponse exchangeCodeToJwt(@RequestParam("code") String code) {
		return ssoService.tokens(code);
	}

	@PutMapping("/jwt")
	TemporaryCodeResponse exchangeJwtToCode(Principal currentUser) {
		return TemporaryCodeResponse.builder()
			.code(
				ssoService.temporaryCodeByAccount(currentUser.getName())
			)
		.build();
	}
}
