package com.github.bogdanovmn.authservice.feature.oauth2;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(OAuth2Properties.class)
class OAuth2Config {

	@Bean
	HttpClient oAuth2HttpClient() {
		return HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();
	}

	@Bean
	Cache<String, OAuth2State> oauth2StateCache(OAuth2Properties properties) {
		return Caffeine.newBuilder()
			.expireAfterWrite(properties.getStateTtlInSeconds(), TimeUnit.SECONDS)
			.maximumSize(1000)
			.build();
	}
}
