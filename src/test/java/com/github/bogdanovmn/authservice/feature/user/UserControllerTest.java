package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.common.RSAKey;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtFactory;
import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		UserController.class
	}
)
class UserControllerTest extends AbstractControllerTest {
	@MockBean
	private UserService userService;

	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void usersListIsForbiddenForAnonymous() throws Exception {
		this.mockMvc.perform(
			get("/users")
		).andExpect(status().isForbidden());
	}

	@Test
	void usersListIsAvailableForAdmin() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));
		when(userService.all())
			.thenReturn(
				List.of(
					UserResponse.builder()
						.id(userId)
						.name("Joe")
						.email("joe@mail.ru")
						.status("CREATED")
						.createdAt(new Date())
						.updatedAt(new Date())
						.build()
				)
			);

		MvcResult requestResult = this.mockMvc.perform(
			get("/users")
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andReturn();

		List<UserResponse> response = jsonMapper.readValue(
			requestResult.getResponse().getContentAsString(),
			jsonMapper.getTypeFactory().constructCollectionType(ArrayList.class, UserResponse.class)
		);
		org.junit.jupiter.api.Assertions.assertEquals(
			userId,
			response.get(0).getId()
		);
	}

	@Test
	void createPasswordResetLinkIsForbiddenForAnonymous() throws Exception {
		this.mockMvc.perform(
			post("/users/%s/password-reset".formatted(UUID.randomUUID()))
		).andExpect(status().isForbidden());
	}

	@Test
	void createPasswordResetLinkForAdmin() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));
		when(userService.createResetLink(userId))
			.thenReturn(
				PasswordResetLinkResponse.builder()
					.token(UUID.randomUUID().toString())
					.expiresAt(new Date())
					.ttlInMinutes(60)
					.build()
			);

		this.mockMvc.perform(
			post("/users/%s/password-reset".formatted(userId))
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isOk());
	}

	@Test
	void changeStatusIsForbiddenForAnonymous() throws Exception {
		this.mockMvc.perform(
			put("/users/%s/status".formatted(UUID.randomUUID()))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"INACTIVE\"}")
		).andExpect(status().isForbidden());
	}

	@Test
	void changeStatusIsForbiddenForNonAdmin() throws Exception {
		this.mockMvc.perform(
			put("/users/%s/status".formatted(UUID.randomUUID()))
				.header("Authorization", bearerToken(Set.of("any:user")))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"INACTIVE\"}")
		).andExpect(status().isForbidden());
	}

	@Test
	void adminChangesUserStatus() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account().setEmail("admin@mail.ru")));
		when(userService.changeStatus(userId, Account.Status.INACTIVE, "admin@mail.ru"))
			.thenReturn(
				UserResponse.builder()
					.id(userId)
					.name("Joe")
					.email("joe@mail.ru")
					.status("INACTIVE")
					.createdAt(new Date())
					.updatedAt(new Date())
					.build()
			);

		this.mockMvc.perform(
			put("/users/%s/status".formatted(userId))
				.header("Authorization", bearerAdminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"INACTIVE\"}")
		)
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").value(userId.toString()))
			.andExpect(jsonPath("$.status").value("INACTIVE"));
	}

	@Test
	void changeStatusWithoutStatusIsBadRequest() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account().setEmail("admin@mail.ru")));

		this.mockMvc.perform(
			put("/users/%s/status".formatted(UUID.randomUUID()))
				.header("Authorization", bearerAdminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}")
		).andExpect(status().isBadRequest());
	}

	@Test
	void changeStatusToUnknownValueIsBadRequest() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account().setEmail("admin@mail.ru")));

		this.mockMvc.perform(
			put("/users/%s/status".formatted(UUID.randomUUID()))
				.header("Authorization", bearerAdminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"status\":\"WHATEVER\"}")
		).andExpect(status().isBadRequest());
	}

	@Test
	void tokenOfInactiveAccountIsRejected() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(
				Optional.of(
					new Account()
						.setId(userId)
						.setEmail("joe@mail.ru")
						.setStatus(Account.Status.INACTIVE)
				)
			);

		this.mockMvc.perform(
			get("/users")
				.header("Authorization", bearerToken(Set.of("any:admin"), userId))
		).andExpect(status().isForbidden());
	}

	@Test
	void refreshTokenCannotBeUsedAsAccessToken() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account().setId(userId)));

		// A refresh token has no 'userName' and no 'roles' claims
		this.mockMvc.perform(
			get("/users")
				.header("Authorization", "Bearer " + jwtFactory.createRefreshToken(
					UUID.randomUUID(), Map.of("userId", userId)
				))
		).andExpect(status().isForbidden());
	}

	@Test
	void tokenWithoutRolesClaimIsRejected() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account().setId(userId)));

		this.mockMvc.perform(
			get("/users")
				.header("Authorization", bearerToken(Map.of("userId", userId, "userName", "admin")))
		).andExpect(status().isForbidden());
	}

	@Test
	void staleTokenIsRejectedAfterPasswordChange() throws Exception {
		final UUID userId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(
				Optional.of(
					new Account()
						.setPasswordChangedAt(new Date())
				)
			);

		this.mockMvc.perform(
			get("/users")
				.header("Authorization", "Bearer " + staleToken(userId))
		).andExpect(status().isForbidden());
	}

	@Test
	void tokenOfUnknownAccountIsRejected() throws Exception {
		when(accountRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

		this.mockMvc.perform(
			get("/users")
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isForbidden());
	}

	private String bearerAdminToken() {
		return bearerToken(Set.of("any:admin"));
	}

	private String bearerToken(Object roles) {
		return bearerToken(roles, UUID.randomUUID());
	}

	private String bearerToken(Object roles, UUID userId) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("userId", userId.toString());
		// The 'userName' claim is the display name; the principal is the account email
		claims.put("userName", "admin");
		if (roles != null) {
			claims.put("roles", roles);
		}
		return "Bearer " + jwtFactory.createToken(claims);
	}

	private String staleToken(UUID userId) {
		long now = System.currentTimeMillis();
		return Jwts.builder()
			.setClaims(
				Map.of(
					"roles", Set.of("any:admin"),
					"userId", userId.toString(),
					"userName", "admin"
				)
			)
			.setIssuedAt(new Date(now - 3600_000))
			.setExpiration(new Date(now + 600_000))
			.setId(UUID.randomUUID().toString())
			.signWith(testPrivateKey())
			.compact();
	}

	private java.security.PrivateKey testPrivateKey() {
		try {
			return RSAKey.ofDER(
				new ClassPathResource("jwt/private.der").getInputStream().readAllBytes()
			).asPrivateKey();
		} catch (java.io.IOException e) {
			throw new IllegalStateException(e);
		}
	}
}