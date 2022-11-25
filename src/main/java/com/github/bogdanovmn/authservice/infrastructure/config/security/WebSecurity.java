package com.github.bogdanovmn.authservice.infrastructure.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {

	private final JwtTokenFilter jwtTokenFilter;

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
			.antMatchers(HttpMethod.POST, "/accounts").anonymous()
			.antMatchers(HttpMethod.POST, "/login").anonymous()
			.antMatchers("/actuator/prometheus").permitAll()
//			.antMatchers("/admin/**").hasAuthority(UserRole.Type.Admin.name())
//			.antMatchers("/invites/**").hasAuthority(UserRole.Type.Invite.name())
			.anyRequest().authenticated();

		http.addFilterBefore(
			jwtTokenFilter,
			UsernamePasswordAuthenticationFilter.class
		);
	}
}
