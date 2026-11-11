package com.github.bogdanovmn.authservice.feature.oauth2;

public class OAuth2LoginException extends RuntimeException {

	public static final String INVALID_STATE = "invalid_state";
	public static final String INVALID_REQUEST = "invalid_request";
	public static final String EMAIL_REQUIRED = "email_required";
	public static final String PROVIDER_ERROR = "provider_error";
	public static final String ACCESS_DENIED = "access_denied";
	public static final String UNKNOWN_PROVIDER = "unknown_provider";
	public static final String ACCOUNT_UNAVAILABLE = "account_unavailable";
	public static final String INTERNAL_ERROR = "internal_error";

	private final String errorCode;

	public OAuth2LoginException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	public String getErrorCode() {
		return errorCode;
	}
}
