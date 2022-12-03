package com.github.bogdanovmn.authservice.feature.token;

import com.github.bogdanovmn.authservice.feature.AccountService;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtFactory;
import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.AccountRepository;
import com.github.bogdanovmn.authservice.model.RefreshToken;
import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import com.github.bogdanovmn.authservice.test.fixture.RoleFixture;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		JwtController.class,
		JwtService.class
	}
)
class JwtControllerTest extends AbstractControllerTest {
	@Autowired
	private JwtService jwtService;

	@Autowired
	private JwtFactory jwtFactory;

	@MockBean
	private AccountService accountService;

	@MockBean
	private AccountRepository accountRepository;

	@Test
	void exchangeIsOk() throws Exception {
		final UUID userId = UUID.randomUUID();
		final String userName = "Joe";
		final String email = "joe@mail.ru";
		final String password = "secret";

		when(accountService.getByEmailAndPassword(email, password))
			.thenReturn(
				Optional.of(
					new Account()
						.setName(userName)
						.setId(userId)
						.setRoles(
							Set.of(
								RoleFixture.standardUser(),
								RoleFixture.moderator("app1")
							)
						)
				)
			);

		final UUID refreshTokenId = UUID.randomUUID();
		when(
			refreshTokenRepository.save(
				argThat(account -> account.getAccount().getId().equals(userId))
			)
		).thenReturn(new RefreshToken().setId(refreshTokenId));

		MvcResult requestResult = this.mockMvc.perform(
				post("/jwt")
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

		JwtResponse response = jsonMapper.readValue(
			requestResult.getResponse().getContentAsString(),
			JwtResponse.class
		);

		// Check JWS token

		Claims tokenBody = jwtFactory.checkSignatureAndReturnClaims(response.getToken()).getBody();
		assertEquals(
			userName,
			tokenBody.get("userName")
		);
		assertEquals(
			userId.toString(),
			tokenBody.get("userId").toString()
		);
		assertEquals(
			Set.of("any:user", "app1:moderator"),
			Set.copyOf(tokenBody.get("roles", List.class))
		);

		// Check Refresh token

		Claims refreshTokenBody = jwtFactory.checkSignatureAndReturnClaims(response.getRefreshToken()).getBody();
		assertEquals(
			userId.toString(),
			refreshTokenBody.get("userId")
		);

		assertEquals(
			refreshTokenId.toString(),
			refreshTokenBody.getId()
		);
	}

	@Test
	void accountNotFound() throws Exception {
		this.mockMvc.perform(
			post("/jwt")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ExchangeCredentialsToJwtRequest.builder()
							.email("joe@mail.ru")
							.password("secret")
						.build()
					)
				)
		).andExpect(status().isNotFound());
	}

	@Test
	void badRequest() throws Exception {
		this.mockMvc.perform(
			post("/jwt")
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
			post("/jwt")
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
			post("/jwt")
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