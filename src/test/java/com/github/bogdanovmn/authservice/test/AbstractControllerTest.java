package com.github.bogdanovmn.authservice.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bogdanovmn.authservice.JwtService;
import com.github.bogdanovmn.authservice.infrastructure.config.GlobalExceptionHandling;
import com.github.bogdanovmn.authservice.infrastructure.config.security.JwtTokenFilter;
import com.github.bogdanovmn.authservice.infrastructure.config.security.WebSecurity;
import com.github.bogdanovmn.authservice.model.RefreshTokenRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@ContextConfiguration(
	classes = {
		JwtService.class,
		JwtTokenFilter.class,
		WebSecurity.class,
		GlobalExceptionHandling.class
	}
)
@ExtendWith(SpringExtension.class)
public abstract class AbstractControllerTest {

	@Autowired
	protected MockMvc mockMvc;

	@Autowired
	protected ObjectMapper jsonMapper;

	@MockBean
	protected RefreshTokenRepository refreshTokenRepository;
}