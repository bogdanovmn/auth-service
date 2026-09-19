package com.github.bogdanovmn.authservice.feature.management;

import com.github.bogdanovmn.authservice.common.domain.Application;
import com.github.bogdanovmn.authservice.common.domain.RoleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicationServiceTest {
	private final ApplicationRepository applicationRepository = mock(ApplicationRepository.class);
	private final RoleRepository roleRepository = mock(RoleRepository.class);
	private final ApplicationService applicationService =
		new ApplicationService(applicationRepository, roleRepository);

	@Test
	void systemApplicationCannotBeCreated() {
		assertThrows(
			IllegalArgumentException.class,
			() -> applicationService.create(
				NewAppRequest.builder().name(Application.ANY_APPLICATION).build()
			)
		);
	}

	@Test
	void systemApplicationCannotBeUpdated() {
		when(applicationRepository.findById(1L))
			.thenReturn(Optional.of(new Application().setName(Application.ANY_APPLICATION)));

		assertThrows(
			IllegalArgumentException.class,
			() -> applicationService.update(
				1L,
				UpdateAppRequest.builder().name("renamed").build()
			)
		);
	}

	@Test
	void regularApplicationCannotBeRenamedToSystemName() {
		when(applicationRepository.findById(1L))
			.thenReturn(Optional.of(new Application().setName("translator")));

		assertThrows(
			IllegalArgumentException.class,
			() -> applicationService.update(
				1L,
				UpdateAppRequest.builder().name(Application.ANY_APPLICATION).build()
			)
		);
	}

	@Test
	void systemApplicationCannotBeDeactivated() {
		when(applicationRepository.findById(1L))
			.thenReturn(Optional.of(new Application().setName(Application.ANY_APPLICATION)));

		assertThrows(
			IllegalArgumentException.class,
			() -> applicationService.deactivate(1L)
		);
	}
}