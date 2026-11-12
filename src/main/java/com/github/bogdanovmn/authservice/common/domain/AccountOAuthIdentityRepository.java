package com.github.bogdanovmn.authservice.common.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
class AccountOAuthIdentityRepository {
	private static final RowMapper<AccountOAuthIdentity> IDENTITY_ROW_MAPPER = (rs, rowNum) -> AccountOAuthIdentity.builder()
		.id(rs.getObject("id", UUID.class))
		.accountId(rs.getObject("account_id", UUID.class))
		.provider(rs.getString("provider"))
		.providerUserId(rs.getString("provider_user_id"))
		.email(rs.getString("email"))
		.name(rs.getString("name"))
		.createdAt(rs.getTimestamp("created_at"))
		.updatedAt(rs.getTimestamp("updated_at"))
		.build();

	private final NamedParameterJdbcTemplate jdbc;

	Optional<AccountOAuthIdentity> findByProviderAndProviderUserId(String provider,
																  String providerUserId)
	{
		return jdbc.query("""
					SELECT
						id, account_id, provider, provider_user_id, email, name,
						created_at, updated_at
					FROM account_oauth_identity
					WHERE provider = :provider AND provider_user_id = :providerUserId
				""",
			Map.of("provider", provider, "providerUserId", providerUserId),
			IDENTITY_ROW_MAPPER
		).stream()
			.findFirst();
	}

	void create(AccountOAuthIdentity identity) {
		jdbc.update("""
					INSERT INTO account_oauth_identity
						(account_id, provider, provider_user_id, email, name)
					VALUES
						(:accountId, :provider, :providerUserId, :email, :name)
				""",
			new MapSqlParameterSource()
				.addValue("accountId", identity.getAccountId())
				.addValue("provider", identity.getProvider())
				.addValue("providerUserId", identity.getProviderUserId())
				.addValue("email", identity.getEmail())
				.addValue("name", identity.getName())
		);
	}

	void update(AccountOAuthIdentity identity) {
		jdbc.update("""
					UPDATE account_oauth_identity
					SET email = :email,
					    name = :name,
					    updated_at = CURRENT_TIMESTAMP
					WHERE id = :id
				""",
			new MapSqlParameterSource()
				.addValue("id", identity.getId())
				.addValue("email", identity.getEmail())
				.addValue("name", identity.getName())
		);
	}
}
