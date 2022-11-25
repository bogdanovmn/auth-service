package com.github.bogdanovmn.authservice.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<Role, Long> {
	@Query(
		nativeQuery = true,
		value = """
			SELECT r.* FROM role r
			JOIN application a ON r.app_id = a.id
			WHERE r.name = 'user' AND a.name = 'any'
		"""
	)
	Role getBasicUserRole();
}
