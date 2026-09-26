package com.github.bogdanovmn.authservice.infrastructure.config;

import com.github.bogdanovmn.authservice.common.domain.TooManyAttemptsException;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.NoSuchElementException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlingTest {

	private final MockMvc mockMvc = MockMvcBuilders
		.standaloneSetup(new FailingController())
		.setControllerAdvice(new GlobalExceptionHandling())
		.build();

	@Test
	void serverErrorHidesInternalDetails() throws Exception {
		mockMvc.perform(get("/server-error"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.code").value(500))
			.andExpect(jsonPath("$.message").value("Internal server error"))
			.andExpect(jsonPath("$.exception").doesNotExist());
	}

	@Test
	void clientErrorKeepsMessage() throws Exception {
		mockMvc.perform(get("/not-found"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.code").value(404))
			.andExpect(jsonPath("$.message").value("There is no such account"))
			.andExpect(jsonPath("$.exception").value(NoSuchElementException.class.getName()));
	}

	@Test
	void tooManyAttemptsAreReportedWithRetryAfter() throws Exception {
		mockMvc.perform(get("/too-many-attempts"))
			.andExpect(status().isTooManyRequests())
			.andExpect(header().string("Retry-After", "120"))
			.andExpect(jsonPath("$.code").value(429))
			.andExpect(jsonPath("$.message").value("Too many failed attempts. Try again in 2 minutes."))
			.andExpect(jsonPath("$.exception").doesNotExist());
	}

	@Test
	void tooManyAttemptsAreReportedInSeconds() throws Exception {
		mockMvc.perform(get("/too-many-attempts-short"))
			.andExpect(status().isTooManyRequests())
			.andExpect(header().string("Retry-After", "45"))
			.andExpect(jsonPath("$.message").value("Too many failed attempts. Try again in 45 seconds."));
	}

	@RestController
	static class FailingController {

		@GetMapping("/server-error")
		String serverError() {
			throw new IllegalStateException("Connection to jdbc:postgresql://hserver:5432/auth failed");
		}

		@GetMapping("/not-found")
		String notFound() {
			throw new NoSuchElementException("There is no such account");
		}

		@GetMapping("/too-many-attempts")
		String tooManyAttempts() {
			throw new TooManyAttemptsException(120);
		}

		@GetMapping("/too-many-attempts-short")
		String tooManyAttemptsShort() {
			throw new TooManyAttemptsException(45);
		}
	}
}
