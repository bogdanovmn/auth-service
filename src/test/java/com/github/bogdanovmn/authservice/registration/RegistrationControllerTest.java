package com.github.bogdanovmn.authservice.registration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bogdanovmn.authservice.AccountService;
import com.github.bogdanovmn.authservice.JwtService;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtTokenFilter;
import com.github.bogdanovmn.authservice.infrastructure.config.security.WebSecurity;
import com.github.bogdanovmn.authservice.model.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(
	classes = {
		RegistrationController.class,
		JwtService.class,
		JwtTokenFilter.class,
		WebSecurity.class
	}
)
@ExtendWith(SpringExtension.class)
class RegistrationControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper jsonMapper;

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