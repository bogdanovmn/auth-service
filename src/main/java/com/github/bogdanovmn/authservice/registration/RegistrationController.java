package com.github.bogdanovmn.authservice.registration;

import com.github.bogdanovmn.authservice.AccountService;
import com.github.bogdanovmn.authservice.AlreadyExistsException;
import com.github.bogdanovmn.authservice.model.Account;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
class RegistrationController {
	private final RegistrationService registrationService;
	private final AccountService accountService;

	@PostMapping("/accounts")
	public ResponseEntity<?> registration(@RequestBody @Valid RegistrationRequest request) throws AlreadyExistsException {
		Optional<Account> existedUser = accountService.getByEmail(request.getEmail());
		if (existedUser.isPresent()) {
			throw new AlreadyExistsException(existedUser.get().getEmail());
		}

		return ResponseEntity.ok().build();
	}
}
