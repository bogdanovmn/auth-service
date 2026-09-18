package com.github.bogdanovmn.authservice.infrastructure.audit;

import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.infrastructure.web.ClientInfo;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityEventLoggerTest {

	@Test
	void logResolvesClientInfoInRequestThreadAndDelegates() {
		SecurityEventWriter securityEventWriter = mock(SecurityEventWriter.class);
		ClientInfo clientInfo = mock(ClientInfo.class);
		SecurityEventLogger securityEventLogger = new SecurityEventLogger(securityEventWriter, clientInfo);

		final UUID userId = UUID.randomUUID();
		when(clientInfo.ip()).thenReturn("1.2.3.4");
		when(clientInfo.userAgent()).thenReturn("Chrome/1.0");

		securityEventLogger.log(userId, AccountSecurityEventType.LOGIN);

		verify(securityEventWriter).write(userId, AccountSecurityEventType.LOGIN, "1.2.3.4", "Chrome/1.0");
		verify(clientInfo).ip();
		verify(clientInfo).userAgent();
	}

	@Test
	void logUnknownAttemptResolvesClientInfoInRequestThreadAndDelegates() {
		SecurityEventWriter securityEventWriter = mock(SecurityEventWriter.class);
		ClientInfo clientInfo = mock(ClientInfo.class);
		SecurityEventLogger securityEventLogger = new SecurityEventLogger(securityEventWriter, clientInfo);

		when(clientInfo.ip()).thenReturn("5.6.7.8");
		when(clientInfo.userAgent()).thenReturn("Safari/2.0");

		securityEventLogger.logUnknownAttempt("joe@mail.ru");

		verify(securityEventWriter).writeFailedAttempt("joe@mail.ru", "5.6.7.8", "Safari/2.0");
		verify(clientInfo).ip();
		verify(clientInfo).userAgent();
	}
}