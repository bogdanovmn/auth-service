package com.github.bogdanovmn.authservice.feature.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(TemporaryCodeProperties.class)
@RequiredArgsConstructor
class CacheConfig {
	private final TemporaryCodeProperties properties;

	@Bean
	Cache<String, JwtResponse> temporaryCodeCache() {
		return Caffeine.newBuilder()
			.expireAfterWrite(properties.getTtlInSec(), TimeUnit.SECONDS)
			.maximumSize(100)
		.build();
	}
}
