package com.github.bogdanovmn.authservice.feature.activity;

import com.github.bogdanovmn.authservice.common.domain.AccountSecurityEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
class AllEventsService {
	private final AccountSecurityEventRepository accountSecurityEventRepository;

	@Transactional(readOnly = true)
	public List<SecurityEventResponse> last() {
		return accountSecurityEventRepository.last100WithAccount().stream()
			.map(
				row -> SecurityEventResponse.builder()
					.id(row.getEvent().getId())
					.type(row.getEvent().getType().name())
					.ip(row.getEvent().getIp())
					.userAgent(row.getEvent().getUserAgent())
					.clientUrl(row.getEvent().getClientUrl())
					.provider(row.getEvent().getProvider())
					.createdAt(row.getEvent().getCreatedAt())
					.accountId(row.getAccountId())
					.accountName(row.getAccountName())
					.accountEmail(row.getAccountEmail())
					.build()
			)
			.toList();
	}
}
