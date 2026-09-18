package com.github.bogdanovmn.authservice.feature.activity;

import com.github.bogdanovmn.authservice.common.domain.FailedLoginAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class LoginAttemptService {
	private final FailedLoginAttemptRepository failedLoginAttemptRepository;

	@Transactional(readOnly = true)
	public List<LoginAttemptResponse> last() {
		return failedLoginAttemptRepository.findTop100ByOrderByCreatedAtDesc().stream()
			.map(
				attempt -> LoginAttemptResponse.builder()
					.id(attempt.getId())
					.email(attempt.getEmail())
					.ip(attempt.getIp())
					.userAgent(attempt.getUserAgent())
					.createdAt(attempt.getCreatedAt())
					.build()
			)
			.toList();
	}
}