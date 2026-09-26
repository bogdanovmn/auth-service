package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.domain.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;


@Configuration
@EnableWebSecurity
@EnableConfigurationProperties(CorsProperties.class)
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

	private final JwtTokenFilter jwtTokenFilter;
	private final CorsProperties corsProperties;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(username -> null);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// Enable CORS and disable CSRF
		http = http.cors().and().csrf().disable();

		// Set session management to stateless
		http = http
			.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			.and();

		http.authorizeRequests()
			.antMatchers(HttpMethod.POST,   "/accounts").anonymous()
			.antMatchers(HttpMethod.POST,   "/jwt").anonymous()
			.antMatchers(HttpMethod.PUT,    "/jwt").anonymous()
			.antMatchers(HttpMethod.DELETE, "/jwt").authenticated()
			.antMatchers(HttpMethod.POST,   "/sso/code").anonymous()
			.antMatchers(HttpMethod.GET,    "/sso/jwt").anonymous()
			.antMatchers(HttpMethod.PUT,    "/sso/jwt").authenticated()
			.antMatchers(HttpMethod.GET,    "/applications").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.GET,    "/applications/public").authenticated()
			.antMatchers(HttpMethod.POST,   "/applications").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.PUT,    "/applications/*").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.DELETE, "/applications/*").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.GET,    "/users").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.GET,    "/users/*/activity").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.GET,    "/login-attempts").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.POST,   "/users/*/password-reset").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.PUT,    "/users/*/status").hasRole(Role.Name.admin.name())
			.antMatchers(HttpMethod.PUT,    "/password-reset").anonymous()
			.antMatchers("/actuator/prometheus").permitAll()
			.anyRequest().authenticated();

		http.addFilterBefore(
			jwtTokenFilter,
			UsernamePasswordAuthenticationFilter.class
		);
	}

	@Bean
	GrantedAuthorityDefaults grantedAuthorityDefaults() {
		return new GrantedAuthorityDefaults(""); // Remove the ROLE_ prefix
	}

	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source =
			new UrlBasedCorsConfigurationSource();
		CorsConfiguration config = new CorsConfiguration();
		// Explicit list of frontends: "*" together with credentials is rejected by
		// browsers anyway, and would allow any page to call the API on behalf of a user
		config.setAllowedOrigins(corsProperties.getAllowedOrigins());
		config.setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns());
		config.setAllowedMethods(corsProperties.getAllowedMethods());
		config.setAllowedHeaders(corsProperties.getAllowedHeaders());
		config.setExposedHeaders(corsProperties.getExposedHeaders());
		config.setAllowCredentials(corsProperties.isAllowCredentials());
		config.setMaxAge(corsProperties.getMaxAge());
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}
