package com.github.bogdanovmn.authservice.infrastructure.audit;

import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEvent;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventRepository;
import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventType;
import com.github.bogdanovmn.authservice.common.domain.AccountRepository;
import com.github.bogdanovmn.authservice.common.domain.FailedLoginAttempt;
import com.github.bogdanovmn.authservice.common.domain.FailedLoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityEventWriter {
	private final AccountSecurityEventRepository accountSecurityEventRepository;
	private final FailedLoginAttemptRepository failedLoginAttemptRepository;
	private final AccountRepository accountRepository;

	@Async("securityEventExecutor")
	public void write(UUID accountId, AccountSecurityEventType type, String ip, String userAgent) {
		accountSecurityEventRepository.save(
			new AccountSecurityEvent()
				.setAccount(accountRepository.getReferenceById(accountId))
				.setType(type)
				.setIp(ip)
				.setUserAgent(userAgent)
		);
		log.debug("Security event recorded for account {}: {}", accountId, type);
	}

	@Async("securityEventExecutor")
	public void writeFailedAttempt(String email, String ip, String userAgent) {
		failedLoginAttemptRepository.save(
			new FailedLoginAttempt()
				.setEmail(email)
				.setIp(ip)
				.setUserAgent(userAgent)
		);
		log.debug("Failed login attempt recorded for email {}", email);
	}
}