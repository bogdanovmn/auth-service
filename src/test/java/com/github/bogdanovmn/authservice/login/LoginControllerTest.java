package com.github.bogdanovmn.authservice.login;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bogdanovmn.authservice.AccountService;
import com.github.bogdanovmn.authservice.JwtService;
import com.github.bogdanovmn.authservice.fixture.RoleFixture;
import com.github.bogdanovmn.authservice.infrastructure.config.GlobalExceptionHandling;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtTokenFilter;
import com.github.bogdanovmn.authservice.infrastructure.config.security.WebSecurity;
import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.AccountRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(
	classes = {
		LoginController.class,
		JwtService.class,
		JwtTokenFilter.class,
		WebSecurity.class,
		GlobalExceptionHandling.class
	}
)
@ExtendWith(SpringExtension.class)
class LoginControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper jsonMapper;

	@Autowired
	private JwtService jwtService;

	@MockBean
	private AccountService accountService;

	@MockBean
	private AccountRepository accountRepository;

	@Test
	void login() throws Exception {
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

		MvcResult requestResult = this.mockMvc.perform(
				post("/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(
						jsonMapper.writeValueAsString(
							LoginRequest.builder()
								.email(email)
								.password(password)
								.build()
						)
					)
			).andExpect(status().isOk())
			.andExpect(content().contentType(MediaType.APPLICATION_JSON))
			.andReturn();

		String token = jsonMapper.readValue(
			requestResult.getResponse().getContentAsString(),
			LoginResponse.class
		).getToken();

		Claims tokenBody = jwtService.parse(token).getBody();
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
	}

	@Test
	void loginNotFound() throws Exception {
		this.mockMvc.perform(
			post("/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						LoginRequest.builder()
							.email("joe@mail.ru")
							.password("secret")
							.build()
					)
				)
		).andExpect(status().isNotFound());
	}
}