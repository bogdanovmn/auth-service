package com.github.bogdanovmn.authservice.feature.token;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "cache.temporary-code")
class TemporaryCodeProperties {
	private int ttlInSec = 10;
}
