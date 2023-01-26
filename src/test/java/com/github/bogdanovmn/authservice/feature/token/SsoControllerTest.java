package com.github.bogdanovmn.authservice.feature.token;

import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		SsoController.class
	}
)
class SsoControllerTest extends AbstractControllerTest {
	@MockBean
	private SsoService ssoService;

	@Test
	void exchangeCredentialsToCode() throws Exception {
		final String code = UUID.randomUUID().toString();
		final String email = "joe@mail.ru";
		final String password = "secret";

		final UUID refreshTokenId = UUID.randomUUID();
		when(ssoService.temporaryCode(email, password))
			.thenReturn(code);

		MvcResult requestResult = this.mockMvc.perform(
			post("/sso/code")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ExchangeCredentialsToJwtRequest.builder()
							.email(email)
							.password(password)
						.build()
					)
				)
		).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andReturn();

		TemporaryCodeResponse response = jsonMapper.readValue(
			requestResult.getResponse().getContentAsString(),
			TemporaryCodeResponse.class
		);

		assertEquals(
			code,
			response.getCode()
		);
	}


	@Test
	void badRequest() throws Exception {
		this.mockMvc.perform(
			post("/sso/code")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ExchangeCredentialsToJwtRequest.builder()
							.email("joe@mail.ru")
							.password("")
						.build()
					)
				)
		).andExpect(status().isBadRequest());

		this.mockMvc.perform(
			post("/sso/code")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ExchangeCredentialsToJwtRequest.builder()
							.email("")
							.password("secret")
						.build()
					)
				)
		).andExpect(status().isBadRequest());

		this.mockMvc.perform(
			post("/sso/code")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ExchangeCredentialsToJwtRequest.builder()
							.email("joe@mail.ru")
						.build()
					)
				)
		).andExpect(status().isBadRequest());
	}
}