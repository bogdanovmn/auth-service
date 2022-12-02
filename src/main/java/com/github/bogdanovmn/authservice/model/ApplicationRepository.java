package com.github.bogdanovmn.authservice.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Long> {

	Optional<Application> getByName(String name);

	@Query(
		nativeQuery = true,
		value = """
			SELECT
				a.id appId, a.name appName,
				r.name roleName,
				COUNT(ar.role_id) usersCount
			FROM application a
			LEFT JOIN role r ON r.app_id = a.id
			LEFT JOIN account2role ar ON ar.role_id = r.id
			GROUP BY a.id, a.name, r.name
			ORDER BY a.name;
		"""
	)
	List<ApplicationStatisticQueryResult> getApplicationsStatistic();

	interface ApplicationStatisticQueryResult {
		long getAppId();
		String getAppName();
		String getRoleName();
		int getUsersCount();
	}
}
