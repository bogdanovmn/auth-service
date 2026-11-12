package com.github.bogdanovmn.authservice.feature.management;

import com.github.bogdanovmn.authservice.common.domain.Application;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
class ApplicationRepository {
	private static final RowMapper<Application> APPLICATION_ROW_MAPPER = (rs, rowNum) -> Application.builder()
		.id(rs.getLong("id"))
		.name(rs.getString("name"))
		.shortDescription(rs.getString("short_description"))
		.url(rs.getString("url"))
		.active(rs.getBoolean("active"))
		.createdAt(rs.getTimestamp("created_at"))
		.build();

	private static final RowMapper<ApplicationStatisticRow> STATISTIC_ROW_MAPPER = (rs, rowNum) -> ApplicationStatisticRow.builder()
		.appId(rs.getLong("app_id"))
		.appName(rs.getString("app_name"))
		.shortDescription(rs.getString("short_description"))
		.url(rs.getString("url"))
		.active(rs.getBoolean("active"))
		.roleName(rs.getString("role_name"))
		.usersCount(rs.getInt("users_count"))
		.build();

	private final NamedParameterJdbcTemplate jdbc;

	Optional<Application> findById(Long id) {
		return jdbc.query("""
					SELECT id, name, short_description, url, active, created_at
					FROM application
					WHERE id = :id
				""",
			Map.of("id", id),
			APPLICATION_ROW_MAPPER
		).stream()
			.findFirst();
	}

	Optional<Application> getByName(String name) {
		return jdbc.query("""
					SELECT id, name, short_description, url, active, created_at
					FROM application
					WHERE name = :name
				""",
			Map.of("name", name),
			APPLICATION_ROW_MAPPER
		).stream()
			.findFirst();
	}

	List<Application> findAll() {
		return jdbc.query("""
					SELECT id, name, short_description, url, active, created_at
					FROM application
					ORDER BY name
				""",
			APPLICATION_ROW_MAPPER
		);
	}

	List<ApplicationStatisticRow> getApplicationsStatistic() {
		return jdbc.query("""
					SELECT
						a.id app_id, a.name app_name, a.short_description, a.url, a.active,
						r.name role_name,
						COUNT(ar.role_id) users_count
					FROM application a
					LEFT JOIN role r ON r.app_id = a.id
					LEFT JOIN account2role ar ON ar.role_id = r.id
					GROUP BY a.id, a.name, a.short_description, a.url, a.active, r.name
					ORDER BY a.name
				""",
			STATISTIC_ROW_MAPPER
		);
	}

	Long create(String name) {
		return jdbc.queryForObject("""
					INSERT INTO application (name)
					VALUES (:name)
					RETURNING id
				""",
			Map.of("name", name),
			Long.class
		);
	}

	void update(Long id, String name, String shortDescription, String url) {
		jdbc.update("""
					UPDATE application
					SET name = :name,
					    short_description = :shortDescription,
					    url = :url
					WHERE id = :id
				""",
			new MapSqlParameterSource()
				.addValue("id", id)
				.addValue("name", name)
				.addValue("shortDescription", shortDescription)
				.addValue("url", url)
		);
	}

	void deactivate(Long id) {
		jdbc.update(
			"UPDATE application SET active = FALSE WHERE id = :id",
			Map.of("id", id)
		);
	}
}
