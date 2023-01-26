package com.github.bogdanovmn.authservice.feature.token;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
class CacheConfig {
	@Value("${cache.temporary-code.ttl-in-sec:10}")
	private int codeTtlInSec;

	@Bean
	Cache<String, JwtResponse> temporaryCodeCache() {
		return Caffeine.newBuilder()
			.expireAfterWrite(codeTtlInSec, TimeUnit.SECONDS)
			.maximumSize(100)
		.build();
	}
}
