package com.github.bogdanovmn.authservice.feature.token;

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
public class RefreshTokenRepository {
	private static final RowMapper<RefreshToken> REFRESH_TOKEN_ROW_MAPPER = (rs, rowNum) -> RefreshToken.builder()
		.id(rs.getObject("id", UUID.class))
		.accountId(rs.getObject("account_id", UUID.class))
		.createdAt(rs.getTimestamp("created_at"))
		.expiresAt(rs.getTimestamp("expires_at"))
		.build();

	private final NamedParameterJdbcTemplate jdbc;

	public Optional<RefreshToken> findById(UUID id) {
		return jdbc.query("""
					SELECT id, account_id, created_at, expires_at
					FROM refresh_token
					WHERE id = :id
				""",
			Map.of("id", id),
			REFRESH_TOKEN_ROW_MAPPER
		).stream()
			.findFirst();
	}

	public Optional<RefreshToken> findByAccountId(UUID accountId) {
		return jdbc.query("""
					SELECT id, account_id, created_at, expires_at
					FROM refresh_token
					WHERE account_id = :accountId
				""",
			Map.of("accountId", accountId),
			REFRESH_TOKEN_ROW_MAPPER
		).stream()
			.findFirst();
	}

	public UUID create(UUID accountId, Date expiresAt) {
		return jdbc.queryForObject("""
					INSERT INTO refresh_token (account_id, expires_at)
					VALUES (:accountId, :expiresAt)
					RETURNING id
				""",
			Map.of("accountId", accountId, "expiresAt", expiresAt),
			UUID.class
		);
	}

	public void deleteById(UUID id) {
		jdbc.update(
			"DELETE FROM refresh_token WHERE id = :id",
			Map.of("id", id)
		);
	}
}
