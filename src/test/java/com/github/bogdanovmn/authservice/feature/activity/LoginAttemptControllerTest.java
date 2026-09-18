package com.github.bogdanovmn.authservice.feature.activity;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtFactory;
import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		LoginAttemptController.class
	}
)
class LoginAttemptControllerTest extends AbstractControllerTest {
	@MockBean
	private LoginAttemptService loginAttemptService;

	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void lastAttemptsIsForbiddenForAnonymous() throws Exception {
		this.mockMvc.perform(
			get("/login-attempts")
		).andExpect(status().isForbidden());
	}

	@Test
	void lastAttemptsIsAvailableForAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));
		when(loginAttemptService.last())
			.thenReturn(
				List.of(
					LoginAttemptResponse.builder()
						.id(123L)
						.email("hacker@mail.ru")
						.ip("9.9.9.9")
						.userAgent("curl/8.0")
						.createdAt(new Date())
						.build()
				)
			);

		MvcResult requestResult = this.mockMvc.perform(
			get("/login-attempts")
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andReturn();

		List<LoginAttemptResponse> response = jsonMapper.readValue(
			requestResult.getResponse().getContentAsString(),
			jsonMapper.getTypeFactory().constructCollectionType(ArrayList.class, LoginAttemptResponse.class)
		);
		org.junit.jupiter.api.Assertions.assertEquals(
			"hacker@mail.ru",
			response.get(0).getEmail()
		);
	}

	private String bearerAdminToken() {
		return "Bearer " + jwtFactory.createToken(
			Map.of(
				"roles", Set.of("any:admin"),
				"userId", UUID.randomUUID().toString(),
				"userName", "admin"
			)
		);
	}
}