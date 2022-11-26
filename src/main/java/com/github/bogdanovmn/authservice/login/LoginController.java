package com.github.bogdanovmn.authservice.login;

import com.github.bogdanovmn.authservice.AccountService;
import com.github.bogdanovmn.authservice.JwtService;
import com.github.bogdanovmn.authservice.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
class LoginController {
	private final AccountService accountService;
	private final JwtService jwtService;

	@PostMapping("/login")
	LoginResponse login(@RequestBody @Valid LoginRequest request) {
		Account account = accountService.getByEmailAndPassword(request.getEmail(), request.getPassword())
			.orElseThrow(NoSuchElementException::new);

		return LoginResponse.builder()
			.token(jwtService.createToken(account))
			.refreshToken(
				jwtService.createRefreshToken(account)
			)
		.build();
	}
}
