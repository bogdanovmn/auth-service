package com.github.bogdanovmn.authservice.feature.activity;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
class AllEventsController {
	private final AllEventsService allEventsService;

	@GetMapping
	public ResponseEntity<List<SecurityEventResponse>> allEvents() {
		return ResponseEntity.ok(
			allEventsService.last()
		);
	}
}
