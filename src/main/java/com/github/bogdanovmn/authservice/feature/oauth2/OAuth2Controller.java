package com.github.bogdanovmn.authservice.feature.oauth2;

import com.github.bogdanovmn.authservice.common.domain.AccountNotAvailableException;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.feature.token.SsoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Slf4j
class OAuth2Controller {
	private final OAuth2Service oAuth2Service;
	private final SsoService ssoService;
	private final OAuth2Properties properties;

	@GetMapping("/authorization/{provider}")
	ResponseEntity<Void> authorization(@PathVariable String provider,
									   @RequestParam(required = false) String from,
									   @RequestParam(required = false) String clientUrl)
	{
		try {
			return redirect(
				oAuth2Service.authorizationUrl(provider, from, clientUrl)
			);
		} catch (OAuth2LoginException ex) {
			log.info("OAuth2 authorization start failed for provider '{}': {}", provider, ex.getMessage());
			return errorRedirect(ex.getErrorCode());
		}
	}

	@GetMapping("/callback/{provider}")
	ResponseEntity<Void> callback(@PathVariable String provider,
								  @RequestParam(required = false) String code,
								  @RequestParam(required = false) String state,
								  @RequestParam(name = "device_id", required = false) String deviceId,
								  @RequestParam(required = false) String error,
								  @RequestParam(name = "error_description", required = false) String errorDescription)
	{
		try {
			OAuth2LoginResult result = oAuth2Service.login(
				provider,
				code,
				state,
				deviceId,
				error,
				errorDescription
			);
			String temporaryCode = ssoService.temporaryCodeByAccount(
				result.getAccount().getEmail(),
				AccountSecurityEventType.OAUTH_LOGIN,
				provider,
				result.getClientUrl()
			);
			return redirect(
				result.getFrom() != null
					? appendCode(result.getFrom(), temporaryCode)
					: appendCode(frontendCallbackUrl(), temporaryCode)
			);
		} catch (OAuth2LoginException ex) {
			log.info("OAuth2 login failed for provider '{}': {}", provider, ex.getMessage());
			return errorRedirect(ex.getErrorCode());
		} catch (AccountNotAvailableException ex) {
			log.info("OAuth2 login rejected for provider '{}': account is not available", provider);
			return errorRedirect(OAuth2LoginException.ACCOUNT_UNAVAILABLE);
		} catch (Exception ex) {
			log.error("OAuth2 login failed for provider '{}'", provider, ex);
			return errorRedirect(OAuth2LoginException.INTERNAL_ERROR);
		}
	}

	private ResponseEntity<Void> redirect(String url) {
		return ResponseEntity.status(HttpStatus.FOUND)
			.location(URI.create(url))
			.build();
	}

	private ResponseEntity<Void> errorRedirect(String errorCode) {
		return redirect(
			frontendBaseUrl()
				+ "/login?oauthError="
				+ URLEncoder.encode(errorCode, StandardCharsets.UTF_8)
		);
	}

	private String frontendCallbackUrl() {
		return frontendBaseUrl() + "/oauth2/callback";
	}

	private String frontendBaseUrl() {
		String base = properties.getFrontendUrl();
		if (base == null || base.isBlank()) {
			return "";
		}
		while (base.endsWith("/")) {
			base = base.substring(0, base.length() - 1);
		}
		return base;
	}

	private static String appendCode(String url, String code) {
		return url
			+ (url.contains("?") ? "&" : "?")
			+ "code="
			+ URLEncoder.encode(code, StandardCharsets.UTF_8);
	}
}
