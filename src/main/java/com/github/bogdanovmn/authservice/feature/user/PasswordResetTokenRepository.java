package com.github.bogdanovmn.authservice.feature.user;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class PasswordResetTokenRepository {
	private static final RowMapper<PasswordResetToken> TOKEN_ROW_MAPPER = (rs, rowNum) -> PasswordResetToken.builder()
		.id(rs.getObject("id", UUID.class))
		.accountId(rs.getObject("account_id", UUID.class))
		.createdAt(rs.getTimestamp("created_at"))
		.expiresAt(rs.getTimestamp("expires_at"))
		.build();

	private final NamedParameterJdbcTemplate jdbc;

	Optional<PasswordResetToken> findById(UUID id) {
		return jdbc.query("""
					SELECT id, account_id, created_at, expires_at
					FROM password_reset_token
					WHERE id = :id
				""",
			Map.of("id", id),
			TOKEN_ROW_MAPPER
		).stream()
			.findFirst();
	}

	UUID create(UUID accountId, Date expiresAt) {
		return jdbc.queryForObject("""
					INSERT INTO password_reset_token (account_id, expires_at)
					VALUES (:accountId, :expiresAt)
					RETURNING id
				""",
			Map.of("accountId", accountId, "expiresAt", expiresAt),
			UUID.class
		);
	}

	void deleteById(UUID id) {
		jdbc.update(
			"DELETE FROM password_reset_token WHERE id = :id",
			Map.of("id", id)
		);
	}

	void deleteByAccountId(UUID accountId) {
		jdbc.update(
			"DELETE FROM password_reset_token WHERE account_id = :accountId",
			Map.of("accountId", accountId)
		);
	}
}
