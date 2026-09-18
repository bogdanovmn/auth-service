package com.github.bogdanovmn.authservice.feature.management;

import com.github.bogdanovmn.authservice.common.domain.Role;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Set;

@Value
@Builder
class NewAppRequest {
	String name;
	@Singular
	Set<Role.Name> roles;
}
