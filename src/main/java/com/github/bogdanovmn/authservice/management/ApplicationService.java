package com.github.bogdanovmn.authservice.management;

import com.github.bogdanovmn.authservice.AlreadyExistsException;
import com.github.bogdanovmn.authservice.model.Application;
import com.github.bogdanovmn.authservice.model.ApplicationRepository;
import com.github.bogdanovmn.authservice.model.ApplicationRepository.ApplicationStatisticQueryResult;
import com.github.bogdanovmn.authservice.model.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class ApplicationService {
	private final ApplicationRepository applicationRepository;

	@Transactional
	public void create(NewAppRequest newApp) {
		Optional<Application> existedApp = applicationRepository.getByName(newApp.getName());
		if (existedApp.isPresent()) {
			throw new AlreadyExistsException(existedApp.get().getName());
		}

		applicationRepository.save(
			new Application()
				.setName(newApp.getName())
				.setRoles(
					newApp.getRoles().stream()
						.map(roleName -> new Role().setName(roleName))
						.toList()
				)
		);
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
}
