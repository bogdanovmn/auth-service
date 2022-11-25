package com.github.bogdanovmn.authservice.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
	Optional<Account> findByEmail(String email);

	Optional<Account> findByEmailAndEncodedPassword(String email, String encodedPassword);
}
