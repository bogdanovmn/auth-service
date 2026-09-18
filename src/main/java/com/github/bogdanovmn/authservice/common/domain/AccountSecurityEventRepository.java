package com.github.bogdanovmn.authservice.common.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountSecurityEventRepository extends JpaRepository<AccountSecurityEvent, Long> {
	List<AccountSecurityEvent> findTop100ByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}