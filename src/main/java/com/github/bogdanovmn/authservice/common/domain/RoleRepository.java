package com.github.bogdanovmn.authservice.common.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleRepository {
	private static final RowMapper<Role> ROLE_ROW_MAPPER = (rs, rowNum) -> Role.builder()
		.id(rs.getLong("id"))
		.name(Role.Name.valueOf(rs.getString("name")))
		.application(
			Application.builder()
				.id(rs.getLong("app_id"))
				.name(rs.getString("app_name"))
				.build()
		)
		.createdAt(rs.getTimestamp("created_at"))
		.build();

	private final NamedParameterJdbcTemplate jdbc;

	public Role getBasicUserRole() {
		return basicRole(Role.Name.user);
	}

	public Role getBasicAdminRole() {
		return basicRole(Role.Name.admin);
	}

	public List<Role> findByAccountId(UUID accountId) {
		return jdbc.query("""
					SELECT
						r.id, r.name, r.app_id, r.created_at, a.name app_name
					FROM role r
					JOIN application a ON r.app_id = a.id
					JOIN account2role ar ON ar.role_id = r.id
					WHERE ar.account_id = :accountId
					ORDER BY a.name, r.name
				""",
			Map.of("accountId", accountId),
			ROLE_ROW_MAPPER
		);
	}

	public void create(String name, Long applicationId) {
		jdbc.update("""
					INSERT INTO role (name, app_id)
					VALUES (:name, :applicationId)
				""",
			Map.of("name", name, "applicationId", applicationId)
		);
	}

	private Role basicRole(Role.Name name) {
		return jdbc.queryForObject("""
					SELECT
						r.id, r.name, r.app_id, r.created_at, a.name app_name
					FROM role r
					JOIN application a ON r.app_id = a.id
					WHERE r.name = :name AND a.name = :appName
				""",
			Map.of("name", name.name(), "appName", Application.ANY_APPLICATION),
			ROLE_ROW_MAPPER
		);
	}
}
