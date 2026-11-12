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
		AllEventsController.class
	}
)
class AllEventsControllerTest extends AbstractControllerTest {
	@MockBean
	private AllEventsService allEventsService;

	@Autowired
	private JwtFactory jwtFactory;

	@Test
	void allEventsIsForbiddenForAnonymous() throws Exception {
		this.mockMvc.perform(
			get("/events")
		).andExpect(status().isForbidden());
	}

	@Test
	void allEventsIsAvailableForAdmin() throws Exception {
		final UUID accountId = UUID.randomUUID();
		when(accountRepository.findById(any(UUID.class)))
			.thenReturn(Optional.of(Account.builder().build()));
		when(allEventsService.last())
			.thenReturn(
				List.of(
					SecurityEventResponse.builder()
						.id(123L)
						.type("LOGIN")
						.ip("1.2.3.4")
						.userAgent("Chrome/1.0")
						.createdAt(new Date())
						.accountId(accountId)
						.accountName("Joe")
						.accountEmail("joe@mail.ru")
						.build()
				)
			);

		MvcResult requestResult = this.mockMvc.perform(
			get("/events")
				.header("Authorization", bearerAdminToken())
		).andExpect(status().isOk())
		.andExpect(content().contentType(MediaType.APPLICATION_JSON))
		.andReturn();

		List<SecurityEventResponse> response = jsonMapper.readValue(
			requestResult.getResponse().getContentAsString(),
			jsonMapper.getTypeFactory().constructCollectionType(ArrayList.class, SecurityEventResponse.class)
		);
		org.junit.jupiter.api.Assertions.assertEquals(
			"joe@mail.ru",
			response.get(0).getAccountEmail()
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
