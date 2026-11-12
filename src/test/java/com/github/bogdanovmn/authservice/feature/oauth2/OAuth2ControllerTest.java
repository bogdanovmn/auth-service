package com.github.bogdanovmn.authservice.feature.oauth2;

import com.github.bogdanovmn.authservice.common.domain.Account;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.feature.token.SsoService;
import com.github.bogdanovmn.authservice.test.AbstractControllerTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(
	classes = {
		OAuth2Controller.class
	}
)
class OAuth2ControllerTest extends AbstractControllerTest {
	@MockBean
	private OAuth2Service oAuth2Service;
	@MockBean
	private SsoService ssoService;
	@MockBean
	private OAuth2Properties properties;

	@Test
	void enabledProvidersAreAvailableForAnonymous() throws Exception {
		when(oAuth2Service.enabledProviders()).thenReturn(List.of("vk", "yandex"));

		this.mockMvc.perform(get("/oauth2/providers"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0]").value("vk"))
			.andExpect(jsonPath("$[1]").value("yandex"));
	}

	@Test
	void authorizationRedirectsToProvider() throws Exception {
		when(oAuth2Service.authorizationUrl("github", "https://app.example/cb", null))
			.thenReturn("https://github.com/login/oauth/authorize?client_id=abc");

		this.mockMvc.perform(
			get("/oauth2/authorization/github")
				.param("from", "https://app.example/cb")
		)
			.andExpect(status().isFound())
			.andExpect(header().string(
				"Location",
				"https://github.com/login/oauth/authorize?client_id=abc"
			));
	}

	@Test
	void callbackRedirectsBackToFromWithTemporaryCode() throws Exception {
		Account account = Account.builder()
			.email("joe@mail.ru")
			.build();
		when(oAuth2Service.login("github", "provider-code", "state-1", null, null, null))
			.thenReturn(
				OAuth2LoginResult.builder()
					.account(account)
					.from("https://app.example/cb?state=1")
					.clientUrl("https://app.example/cb?state=1")
					.build()
			);
		when(ssoService.temporaryCodeByAccount(
			"joe@mail.ru",
			AccountSecurityEventType.OAUTH_LOGIN,
			"github",
			"https://app.example/cb?state=1"
		)).thenReturn("code-1");

		this.mockMvc.perform(
			get("/oauth2/callback/github")
				.param("code", "provider-code")
				.param("state", "state-1")
		)
			.andExpect(status().isFound())
			.andExpect(header().string(
				"Location",
				"https://app.example/cb?state=1&code=code-1"
			));
	}

	@Test
	void callbackWithoutFromGoesToFrontendCallback() throws Exception {
		Account account = Account.builder()
			.email("joe@mail.ru")
			.build();
		when(oAuth2Service.login("github", "provider-code", "state-1", null, null, null))
			.thenReturn(
				OAuth2LoginResult.builder()
					.account(account)
					.clientUrl("https://app.example/cb?state=1")
					.build()
			);
		when(properties.getFrontendUrl()).thenReturn("http://front.example/sso/");
		when(ssoService.temporaryCodeByAccount(
			"joe@mail.ru",
			AccountSecurityEventType.OAUTH_LOGIN,
			"github",
			"https://app.example/cb?state=1"
		)).thenReturn("code-1");

		this.mockMvc.perform(
			get("/oauth2/callback/github")
				.param("code", "provider-code")
				.param("state", "state-1")
		)
			.andExpect(status().isFound())
			.andExpect(header().string(
				"Location",
				"http://front.example/sso/oauth2/callback?code=code-1"
			));
	}

	@Test
	void callbackErrorRedirectsToLoginWithErrorCode() throws Exception {
		when(oAuth2Service.login("github", null, "state-1", null, "access_denied", null))
			.thenThrow(
				new OAuth2LoginException(
					OAuth2LoginException.ACCESS_DENIED,
					"access_denied"
				)
			);
		when(properties.getFrontendUrl()).thenReturn("http://front.example/sso/");

		this.mockMvc.perform(
			get("/oauth2/callback/github")
				.param("state", "state-1")
				.param("error", "access_denied")
		)
			.andExpect(status().isFound())
			.andExpect(header().string(
				"Location",
				"http://front.example/sso/login?oauthError=access_denied"
			));
	}
}
