package com.github.bogdanovmn.authservice.feature.management;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.Role;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtFactory;
import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		ApplicationController.class
	}
)
class ApplicationControllerTest extends AbstractControllerTest {
	@MockBean
	private ApplicationService applicationService;

	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void publicServicesAreForbiddenForAnonymous() throws Exception {
		this.mockMvc.perform(
			get("/applications/public")
		).andExpect(status().isForbidden());
	}

	@Test
	void publicServicesAreAvailableForAuthenticatedUser() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));
		when(applicationService.publicServices())
			.thenReturn(
				List.of(
					PublicService.builder()
						.name("translator")
						.shortDescription("Переводчик")
						.url("https://translate.example.com")
						.build()
				)
			);

		this.mockMvc.perform(
			get("/applications/public")
				.header("Authorization", bearerUserToken())
		).andExpect(status().isOk())
			.andExpect(content().string(org.hamcrest.Matchers.containsString("https://translate.example.com")));
	}

	@Test
	void applicationsOverviewIsForbiddenForNonAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			get("/applications")
				.header("Authorization", bearerUserToken())
		).andExpect(status().isForbidden());
	}

	@Test
	void updateIsForbiddenForNonAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			put("/applications/1")
				.header("Authorization", bearerUserToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}")
		).andExpect(status().isForbidden());
	}

	@Test
	void createIsForbiddenForNonAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			post("/applications")
				.header("Authorization", bearerUserToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content("{}")
		).andExpect(status().isForbidden());
	}

	@Test
	void createIsAvailableForAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			post("/applications")
				.header("Authorization", bearerAdminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						NewAppRequest.builder()
							.name("new-app")
							.role(Role.Name.user)
							.build()
					)
				)
		).andExpect(status().isOk());

		verify(applicationService).create(any(NewAppRequest.class));
	}

	@Test
	void deactivateIsForbiddenForNonAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			delete("/applications/1")
				.header("Authorization", bearerUserToken())
		).andExpect(status().isForbidden());
	}

	@Test
	void deactivateIsAvailableForAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			delete("/applications/1")
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isOk());

		verify(applicationService).deactivate(1L);
	}

	@Test
	void updateIsAvailableForAdmin() throws Exception {
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(new Account()));

		this.mockMvc.perform(
			put("/applications/1")
				.header("Authorization", bearerAdminToken())
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						UpdateAppRequest.builder()
							.name("translator")
							.shortDescription("Переводчик")
							.url("https://translate.example.com")
							.build()
					)
				)
		).andExpect(status().isOk());

		verify(applicationService).update(eq(1L), any(UpdateAppRequest.class));
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

	private String bearerUserToken() {
		return "Bearer " + jwtFactory.createToken(
			Map.of(
				"roles", Set.of("any:user"),
				"userId", UUID.randomUUID().toString(),
				"userName", "joe"
			)
		);
	}
}
