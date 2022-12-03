package com.github.bogdanovmn.authservice.feature.registration;

import com.github.bogdanovmn.authservice.feature.AccountService;
import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.AccountRepository;
import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		RegistrationController.class
	}
)
class RegistrationControllerTest extends AbstractControllerTest {
	@MockBean
	private RegistrationService registrationService;

	@MockBean
	private AccountService accountService;

	@MockBean
	private AccountRepository accountRepository;

	@Test
	void registrationIsOk() throws Exception {
		this.mockMvc.perform(
			post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						RegistrationRequest.builder()
							.accountName("Joe")
							.email("joe@mail.ru")
							.password("secret")
						.build()
					)
				)
		).andExpect(status().isOk());
	}

	@Test
	void accountAlreadyExists() throws Exception {
		final String email = "joe@mail.ru";
		when(accountService.getByEmail(email))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						RegistrationRequest.builder()
							.accountName("Joe")
							.email(email)
							.password("secret")
						.build()
					)
				)
		).andExpect(status().isConflict());
	}

	@Test
	void registrationWithoutRequiredValues() throws Exception {
		this.mockMvc.perform(
			post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						RegistrationRequest.builder()
							.accountName("Joe")
							.email("joe@mail.ru")
							.password("")
						.build()
					)
				)
		).andExpect(status().isBadRequest());

		this.mockMvc.perform(
			post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						RegistrationRequest.builder()
							.accountName("Joe")
							.email("")
							.password("password")
						.build()
					)
				)
		).andExpect(status().isBadRequest());

		this.mockMvc.perform(
			post("/accounts")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						RegistrationRequest.builder()
							.accountName("")
							.email("joe@mail.ru")
							.password("password")
						.build()
					)
				)
		).andExpect(status().isBadRequest());
	}
}