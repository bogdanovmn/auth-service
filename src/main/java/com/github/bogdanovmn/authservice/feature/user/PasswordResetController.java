package com.github.bogdanovmn.authservice.feature.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/password-reset")
@RequiredArgsConstructor
class PasswordResetController {
	private final PasswordResetLinkService passwordResetLinkService;

	@PutMapping
	public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
		passwordResetLinkService.applyNewPassword(
			UUID.fromString(request.getToken()),
			request.getPassword()
		);
		return ResponseEntity.ok().build();
	}
}