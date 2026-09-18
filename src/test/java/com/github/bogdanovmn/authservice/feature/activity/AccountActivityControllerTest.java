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
		AccountActivityController.class
	}
)
class AccountActivityControllerTest extends AbstractControllerTest {
	@MockBean
	private AccountActivityService accountActivityService;

	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void userActivityIsForbiddenForAnonymous() throws Exception {
		this.mockMvc.perform(
			get("/users/%s/activity".formatted(UUID.randomUUID()))
		).andExpect(status().isForbidden());
	}

	@Test
	void userActivityIsAvailableForAdmin() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));
		when(accountActivityService.events(userId))
			.thenReturn(
				UserActivityResponse.builder()
					.account(
						UserActivityResponse.AccountInfo.builder()
							.id(userId)
							.name("Joe")
							.email("joe@mail.ru")
							.build()
					)
					.events(
						List.of(
							UserActivityResponse.Event.builder()
								.id(123L)
								.type("LOGIN")
								.ip("1.2.3.4")
								.userAgent("Chrome/1.0")
								.createdAt(new Date())
								.build()
						)
					)
					.build()
			);

		MvcResult requestResult = this.mockMvc.perform(
			get("/users/%s/activity".formatted(userId))
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andReturn();

		org.junit.jupiter.api.Assertions.assertEquals(
			userId,
			jsonMapper.readValue(
				requestResult.getResponse().getContentAsString(),
				UserActivityResponse.class
			).getAccount().getId()
		);
	}

	@Test
	void userActivityNotFound() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));
		when(accountActivityService.events(any(UUID.class)))
			.thenThrow(new java.util.NoSuchElementException("User with id 'x' has not been found"));

		this.mockMvc.perform(
			get("/users/%s/activity".formatted(UUID.randomUUID()))
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isNotFound());
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