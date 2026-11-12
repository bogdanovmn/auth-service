package com.github.bogdanovmn.authservice.common.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountSecurityEventRepository {
	private static final RowMapper<AccountSecurityEvent> EVENT_ROW_MAPPER = (rs, rowNum) -> AccountSecurityEvent.builder()
		.id(rs.getLong("id"))
		.accountId(rs.getObject("account_id", UUID.class))
		.type(AccountSecurityEventType.valueOf(rs.getString("type")))
		.ip(rs.getString("ip"))
		.userAgent(rs.getString("user_agent"))
		.clientUrl(rs.getString("client_url"))
		.provider(rs.getString("provider"))
		.createdAt(rs.getTimestamp("created_at"))
		.build();

	private static final RowMapper<AccountSecurityEventWithAccount> EVENT_WITH_ACCOUNT_ROW_MAPPER =
		(rs, rowNum) -> AccountSecurityEventWithAccount.builder()
			.event(EVENT_ROW_MAPPER.mapRow(rs, rowNum))
			.accountId(rs.getObject("account_id", UUID.class))
			.accountName(rs.getString("account_name"))
			.accountEmail(rs.getString("account_email"))
			.build();

	private final NamedParameterJdbcTemplate jdbc;

	public List<AccountSecurityEvent> lastByAccountId(UUID accountId) {
		return jdbc.query("""
					SELECT
						id, account_id, type, ip, user_agent, client_url, provider, created_at
					FROM account_security_event
					WHERE account_id = :accountId
					ORDER BY created_at DESC
					LIMIT 100
				""",
			Map.of("accountId", accountId),
			EVENT_ROW_MAPPER
		);
	}

	public List<AccountSecurityEventWithAccount> last100WithAccount() {
		return jdbc.query("""
					SELECT
						e.id, e.account_id, e.type, e.ip, e.user_agent, e.client_url, e.provider, e.created_at,
						a.name AS account_name,
						a.email AS account_email
					FROM account_security_event e
					JOIN account a ON a.id = e.account_id
					ORDER BY e.created_at DESC, e.id DESC
					LIMIT 100
				""",
			Map.of(),
			EVENT_WITH_ACCOUNT_ROW_MAPPER
		);
	}

	public void create(UUID accountId,
					   AccountSecurityEventType type,
					   String ip,
					   String userAgent,
					   String provider,
					   String clientUrl)
	{
		jdbc.update("""
					INSERT INTO account_security_event
						(account_id, type, ip, user_agent, provider, client_url)
					VALUES
						(:accountId, :type, :ip, :userAgent, :provider, :clientUrl)
				""",
			new MapSqlParameterSource()
				.addValue("accountId", accountId)
				.addValue("type", type.name())
				.addValue("ip", ip)
				.addValue("userAgent", userAgent)
				.addValue("provider", provider)
				.addValue("clientUrl", clientUrl)
		);
	}
}
