package com.github.bogdanovmn.authservice.feature.management;

import com.github.bogdanovmn.authservice.common.domain.AlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/applications")
class ApplicationController {
	private final ApplicationService applicationService;

	@PostMapping
	public ResponseEntity<?> addApp(@RequestBody @Valid NewAppRequest newApp) throws AlreadyExistsException {
		applicationService.create(newApp);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public ResponseEntity<?> allApplications() {
		return ResponseEntity.ok(
			applicationService.allWithRoles()
		);
	}

	@GetMapping("/public")
	public ResponseEntity<?> publicServices() {
		return ResponseEntity.ok(
			applicationService.publicServices()
		);
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateApp(@PathVariable Long id, @RequestBody @Valid UpdateAppRequest app) {
		applicationService.update(id, app);
		return ResponseEntity.ok().build();
	}
}
