package com.github.bogdanovmn.authservice.feature.oauth2;

public interface OAuth2ProviderClient {

	String getName();

	boolean supportsPkce(OAuth2Properties.Provider config);

	String buildAuthorizationUrl(OAuth2AuthorizationRequest request);

	OAuth2TokenResponse exchangeCode(OAuth2AuthorizationRequest request, String code, String codeVerifier, String deviceId);

	OAuth2UserInfo fetchUserInfo(OAuth2AuthorizationRequest request, String accessToken);
}
