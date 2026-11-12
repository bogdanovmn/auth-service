package com.github.bogdanovmn.authservice.common.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FailedLoginAttemptRepository {
	private static final RowMapper<FailedLoginAttempt> ATTEMPT_ROW_MAPPER = (rs, rowNum) -> FailedLoginAttempt.builder()
		.id(rs.getLong("id"))
		.email(rs.getString("email"))
		.ip(rs.getString("ip"))
		.userAgent(rs.getString("user_agent"))
		.createdAt(rs.getTimestamp("created_at"))
		.build();

	private final NamedParameterJdbcTemplate jdbc;

	public List<FailedLoginAttempt> last100() {
		return jdbc.query("""
					SELECT
						id, email, ip, user_agent, created_at
					FROM failed_login_attempt
					ORDER BY created_at DESC
					LIMIT 100
				""",
			ATTEMPT_ROW_MAPPER
		);
	}

	public void create(String email, String ip, String userAgent) {
		jdbc.update("""
					INSERT INTO failed_login_attempt (email, ip, user_agent)
					VALUES (:email, :ip, :userAgent)
				""",
			new MapSqlParameterSource()
				.addValue("email", email)
				.addValue("ip", ip)
				.addValue("userAgent", userAgent)
		);
	}
}
