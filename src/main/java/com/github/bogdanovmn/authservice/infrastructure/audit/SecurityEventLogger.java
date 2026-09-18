package com.github.bogdanovmn.authservice.infrastructure.audit;

import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.infrastructure.web.ClientInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityEventLogger {
	private final SecurityEventWriter securityEventWriter;
	private final ClientInfo clientInfo;

	public void log(UUID accountId, AccountSecurityEventType type) {
		securityEventWriter.write(
			accountId,
			type,
			clientInfo.ip(),
			clientInfo.userAgent()
		);
	}

	public void logUnknownAttempt(String email) {
		securityEventWriter.writeFailedAttempt(
			email,
			clientInfo.ip(),
			clientInfo.userAgent()
		);
	}
}