package com.github.bogdanovmn.authservice.feature.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
class UserController {
	private final UserService userService;

	@GetMapping
	public ResponseEntity<List<UserResponse>> allUsers() {
		return ResponseEntity.ok(
			userService.all()
		);
	}

	@PostMapping("/{id}/password-reset")
	public ResponseEntity<PasswordResetLinkResponse> createPasswordResetLink(@PathVariable UUID id) {
		return ResponseEntity.ok(
			userService.createResetLink(id)
		);
	}
}