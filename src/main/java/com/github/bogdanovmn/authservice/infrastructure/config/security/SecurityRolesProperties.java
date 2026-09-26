package com.github.bogdanovmn.authservice.infrastructure.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "security.roles")
public class SecurityRolesProperties {
	@NotBlank
	private String applicationIdPrefix;
}
