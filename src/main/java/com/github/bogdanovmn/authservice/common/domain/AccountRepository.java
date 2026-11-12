package com.github.bogdanovmn.authservice.common.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountRepository {
	private static final RowMapper<Account> ACCOUNT_ROW_MAPPER = (rs, rowNum) -> Account.builder()
		.id(rs.getObject("id", UUID.class))
		.email(rs.getString("email"))
		.name(rs.getString("name"))
		.encodedPassword(rs.getString("encoded_password"))
		.status(Account.Status.valueOf(rs.getString("status")))
		.createdAt(rs.getTimestamp("created_at"))
		.updatedAt(rs.getTimestamp("updated_at"))
		.passwordChangedAt(rs.getTimestamp("password_changed_at"))
		.build();

	private final NamedParameterJdbcTemplate jdbc;
	private final RoleRepository roleRepository;

	public Optional<Account> findById(UUID id) {
		return jdbc.query("""
					SELECT
						id, email, name, encoded_password, status,
						created_at, updated_at, password_changed_at
					FROM account
					WHERE id = :id
				""",
			Map.of("id", id),
			ACCOUNT_ROW_MAPPER
		).stream()
			.findFirst()
			.map(this::withRoles);
	}

	public Optional<Account> findByEmail(String email) {
		return jdbc.query("""
					SELECT
						id, email, name, encoded_password, status,
						created_at, updated_at, password_changed_at
					FROM account
					WHERE email = :email
				""",
			Map.of("email", email),
			ACCOUNT_ROW_MAPPER
		).stream()
			.findFirst()
			.map(this::withRoles);
	}

	public Optional<Account> findByEmailIgnoreCase(String email) {
		return jdbc.query("""
					SELECT
						id, email, name, encoded_password, status,
						created_at, updated_at, password_changed_at
					FROM account
					WHERE LOWER(email) = LOWER(:email)
				""",
			Map.of("email", email),
			ACCOUNT_ROW_MAPPER
		).stream()
			.findFirst()
			.map(this::withRoles);
	}

	public List<Account> findAllOrderByCreatedAtDesc() {
		return jdbc.query("""
					SELECT
						id, email, name, encoded_password, status,
						created_at, updated_at, password_changed_at
					FROM account
					ORDER BY created_at DESC
				""",
			ACCOUNT_ROW_MAPPER
		).stream()
			.map(this::withRoles)
			.toList();
	}

	public long count() {
		return jdbc.queryForObject(
			"SELECT COUNT(*) FROM account",
			Map.of(),
			Long.class
		);
	}

	public Account create(Account account) {
		UUID id = jdbc.queryForObject("""
					INSERT INTO account (email, name, encoded_password, status)
					VALUES (:email, :name, :encodedPassword, :status)
					RETURNING id
				""",
			Map.of(
				"email", account.getEmail(),
				"name", account.getName(),
				"encodedPassword", account.getEncodedPassword(),
				"status", account.getStatus().name()
			),
			UUID.class
		);

		account.getRoles().forEach(
			role -> jdbc.update("""
						INSERT INTO account2role (account_id, role_id)
						VALUES (:accountId, :roleId)
					""",
				Map.of("accountId", id, "roleId", role.getId())
			)
		);

		return account.toBuilder()
			.id(id)
			.build();
	}

	public void updateStatus(UUID id, Account.Status status) {
		jdbc.update("""
					UPDATE account
					SET status = :status,
					    updated_at = CURRENT_TIMESTAMP
					WHERE id = :id
				""",
			Map.of("id", id, "status", status.name())
		);
	}

	public void updatePassword(UUID id, String encodedPassword, Date passwordChangedAt) {
		jdbc.update("""
					UPDATE account
					SET encoded_password = :encodedPassword,
					    password_changed_at = :passwordChangedAt,
					    updated_at = CURRENT_TIMESTAMP
					WHERE id = :id
				""",
			Map.of(
				"id", id,
				"encodedPassword", encodedPassword,
				"passwordChangedAt", passwordChangedAt
			)
		);
	}

	private Account withRoles(Account account) {
		return account.toBuilder()
			.roles(
				new HashSet<>(
					roleRepository.findByAccountId(account.getId())
				)
			)
			.build();
	}
}
