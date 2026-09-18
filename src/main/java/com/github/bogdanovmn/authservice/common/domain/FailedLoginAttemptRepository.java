package com.github.bogdanovmn.authservice.common.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FailedLoginAttemptRepository extends JpaRepository<FailedLoginAttempt, Long> {
	List<FailedLoginAttempt> findTop100ByOrderByCreatedAtDesc();
}