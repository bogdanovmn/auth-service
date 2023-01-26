package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.model.Role;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

	private final JwtTokenFilter jwtTokenFilter;

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
		config.addAllowedOrigin("*");
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		source.registerCorsConfiguration("/**", config);
		return new CorsFilter(source);
	}
}
