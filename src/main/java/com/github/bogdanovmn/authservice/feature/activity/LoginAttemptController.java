package com.github.bogdanovmn.authservice.feature.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/login-attempts")
@RequiredArgsConstructor
class LoginAttemptController {
	private final LoginAttemptService loginAttemptService;

	@GetMapping
	public ResponseEntity<List<LoginAttemptResponse>> lastAttempts() {
		return ResponseEntity.ok(
			loginAttemptService.last()
		);
	}
}