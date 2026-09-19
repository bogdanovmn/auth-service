package com.github.bogdanovmn.authservice.feature.management;

import com.github.bogdanovmn.authservice.common.domain.AlreadyExistsException;
import com.github.bogdanovmn.authservice.common.domain.Application;
import com.github.bogdanovmn.authservice.common.domain.Role;
import com.github.bogdanovmn.authservice.common.domain.RoleRepository;
import com.github.bogdanovmn.authservice.feature.management.ApplicationRepository.ApplicationStatisticQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ApplicationService {
	private final ApplicationRepository applicationRepository;
	private final RoleRepository roleRepository;

	@Transactional
	public void create(NewAppRequest newApp) {
		if (Application.ANY_APPLICATION.equals(newApp.getName())) {
			throw new IllegalArgumentException(
				"The '%s' application is a system application".formatted(Application.ANY_APPLICATION)
			);
		}
		Optional<Application> existedApp = applicationRepository.getByName(newApp.getName());
		if (existedApp.isPresent()) {
			throw new AlreadyExistsException(existedApp.get().getName());
		}

		Application app = applicationRepository.save(
			new Application()
				.setName(newApp.getName())
		);
		List<Role> roles = newApp.getRoles().stream()
			.map(roleName -> new Role().setName(roleName).setApplication(app))
			.toList();
		app.setRoles(roles);
		roleRepository.saveAll(roles);
	}

	@Transactional(readOnly = true)
	public List<ApplicationStatistic> allWithRoles() {
		return applicationRepository.getApplicationsStatistic().stream()
			.collect(Collectors.groupingBy(ApplicationStatisticQueryResult::getAppId))
			.values()
			.stream().map(stat -> {
				ApplicationStatistic.ApplicationStatisticBuilder result = ApplicationStatistic.builder();
				result.id(stat.get(0).getAppId());
				result.name(stat.get(0).getAppName());
				result.shortDescription(stat.get(0).getShortDescription());
				result.url(stat.get(0).getUrl());
				result.active(stat.get(0).getActive());
				stat.stream()
					.filter(s -> s.getRoleName() != null)
					.forEach(
						s -> result.role(
							new ApplicationStatistic.Role(
								s.getRoleName(), s.getUsersCount()
							)
						)
					);
				return result.build();
			})
			.toList();
	}

	@Transactional(readOnly = true)
	public List<PublicService> publicServices() {
		return applicationRepository.findAll().stream()
			.filter(app -> Boolean.TRUE.equals(app.getActive()))
			.filter(app -> app.getUrl() != null && !app.getUrl().isBlank())
			.map(
				app -> PublicService.builder()
					.name(app.getName())
					.shortDescription(app.getShortDescription())
					.url(app.getUrl())
					.build()
			)
			.toList();
	}

	@Transactional
	public void update(Long id, UpdateAppRequest request) {
		Application app = applicationRepository.findById(id)
			.orElseThrow(
				() -> new NoSuchElementException("Application with id '%s' has not been found".formatted(id))
			);
		if (Application.ANY_APPLICATION.equals(app.getName())) {
			throw new IllegalArgumentException(
				"The '%s' application is a system application".formatted(Application.ANY_APPLICATION)
			);
		}
		if (Application.ANY_APPLICATION.equals(request.getName())) {
			throw new IllegalArgumentException(
				"The '%s' name is reserved for a system application".formatted(Application.ANY_APPLICATION)
			);
		}
		applicationRepository.getByName(request.getName())
			.filter(another -> !another.getId().equals(id))
			.ifPresent(
				another -> {
					throw new AlreadyExistsException(another.getName());
				}
			);

		app.setName(request.getName());
		app.setShortDescription(request.getShortDescription());
		app.setUrl(request.getUrl());
	}

	@Transactional
	public void deactivate(Long id) {
		Application app = applicationRepository.findById(id)
			.orElseThrow(
				() -> new NoSuchElementException("Application with id '%s' has not been found".formatted(id))
			);
		if (Application.ANY_APPLICATION.equals(app.getName())) {
			throw new IllegalArgumentException(
				"The '%s' application is a system application".formatted(Application.ANY_APPLICATION)
			);
		}
		app.setActive(false);
	}
}
