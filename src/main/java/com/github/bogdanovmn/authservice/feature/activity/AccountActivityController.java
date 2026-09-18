package com.github.bogdanovmn.authservice.feature.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users/{id}/activity")
@RequiredArgsConstructor
class AccountActivityController {
	private final AccountActivityService accountActivityService;

	@GetMapping
	public ResponseEntity<UserActivityResponse> userActivity(@PathVariable UUID id) {
		return ResponseEntity.ok(
			accountActivityService.events(id)
		);
	}
}