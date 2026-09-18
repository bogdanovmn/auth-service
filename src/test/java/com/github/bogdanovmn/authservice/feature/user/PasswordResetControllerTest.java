package com.github.bogdanovmn.authservice.feature.user;

import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;

import java.util.NoSuchElementException;

import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		PasswordResetController.class
	}
)
class PasswordResetControllerTest extends AbstractControllerTest {
	@MockBean
	private PasswordResetLinkService passwordResetLinkService;

	@Test
	void resetPasswordIsOk() throws Exception {
		this.mockMvc.perform(
			put("/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ResetPasswordRequest.builder()
							.token("00000000-0000-0000-0000-000000000001")
							.password("new-secret")
							.build()
					)
				)
		).andExpect(status().isOk());
	}

	@Test
	void badRequest() throws Exception {
		this.mockMvc.perform(
			put("/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ResetPasswordRequest.builder()
							.token("00000000-0000-0000-0000-000000000001")
							.password("")
							.build()
					)
				)
		).andExpect(status().isBadRequest());

		this.mockMvc.perform(
			put("/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ResetPasswordRequest.builder()
							.password("new-secret")
							.build()
					)
				)
		).andExpect(status().isBadRequest());
	}

	@Test
	void invalidTokenIsBadRequest() throws Exception {
		this.mockMvc.perform(
			put("/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ResetPasswordRequest.builder()
							.token("not-a-uuid")
							.password("new-secret")
							.build()
					)
				)
		).andExpect(status().isBadRequest());
	}

	@Test
	void unknownTokenIsNotFound() throws Exception {
		doThrow(new NoSuchElementException("Unknown password reset token"))
			.when(passwordResetLinkService)
			.applyNewPassword(
				java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"new-secret"
			);

		this.mockMvc.perform(
			put("/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ResetPasswordRequest.builder()
							.token("00000000-0000-0000-0000-000000000001")
							.password("new-secret")
							.build()
					)
				)
		).andExpect(status().isNotFound());
	}

	@Test
	void expiredTokenIsBadRequest() throws Exception {
		doThrow(new IllegalArgumentException("The password reset link has expired"))
			.when(passwordResetLinkService)
			.applyNewPassword(
				java.util.UUID.fromString("00000000-0000-0000-0000-000000000001"),
				"new-secret"
			);

		this.mockMvc.perform(
			put("/password-reset")
				.contentType(MediaType.APPLICATION_JSON)
				.content(
					jsonMapper.writeValueAsString(
						ResetPasswordRequest.builder()
							.token("00000000-0000-0000-0000-000000000001")
							.password("new-secret")
							.build()
					)
				)
		).andExpect(status().isBadRequest());
	}
}