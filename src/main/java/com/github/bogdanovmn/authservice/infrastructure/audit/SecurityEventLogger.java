package com.github.bogdanovmn.authservice.infrastructure.audit;

import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.infrastructure.web.ClientInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityEventLogger {
	private static final int MAX_CLIENT_URL_LENGTH = 1024;

	private final SecurityEventWriter securityEventWriter;
	private final ClientInfo clientInfo;

	public void log(UUID accountId, AccountSecurityEventType type) {
		log(accountId, type, null, null);
	}

	public void log(UUID accountId,
					AccountSecurityEventType type,
					String provider,
					String clientUrl)
	{
		securityEventWriter.write(
			accountId,
			type,
			clientInfo.ip(),
			clientInfo.userAgent(),
			provider,
			withoutQuery(clientUrl)
		);
	}

	public void logUnknownAttempt(String email) {
		securityEventWriter.writeFailedAttempt(
			email,
			clientInfo.ip(),
			clientInfo.userAgent()
		);
	}

	private static String withoutQuery(String url) {
		if (url == null) {
			return null;
		}
		String value = url.trim();
		int cut = value.length();
		int query = value.indexOf('?');
		if (query >= 0 && query < cut) {
			cut = query;
		}
		int fragment = value.indexOf('#');
		if (fragment >= 0 && fragment < cut) {
			cut = fragment;
		}
		value = value.substring(0, cut);
		if (value.isBlank()) {
			return null;
		}
		return value.length() > MAX_CLIENT_URL_LENGTH
			? value.substring(0, MAX_CLIENT_URL_LENGTH)
			: value;
	}
}
