package com.github.bogdanovmn.authservice.feature.oauth2;

class OAuth2LoginException extends RuntimeException {

	static final String INVALID_STATE = "invalid_state";
	static final String INVALID_REQUEST = "invalid_request";
	static final String EMAIL_REQUIRED = "email_required";
	static final String PROVIDER_ERROR = "provider_error";
	static final String ACCESS_DENIED = "access_denied";
	static final String UNKNOWN_PROVIDER = "unknown_provider";
	static final String ACCOUNT_UNAVAILABLE = "account_unavailable";
	static final String INTERNAL_ERROR = "internal_error";

	private final String errorCode;

	OAuth2LoginException(String errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

	String getErrorCode() {
		return errorCode;
	}
}
