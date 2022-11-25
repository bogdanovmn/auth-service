package com.github.bogdanovmn.authservice;

import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.Application;
import com.github.bogdanovmn.authservice.model.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = JwtService.class)
@ExtendWith(SpringExtension.class)
class JwtServiceTest {

	@Autowired
	private JwtService jwtService;

	@Test
	void createToken() {
		String token = jwtService.createToken(
			new Account()
				.setName("Joe")
				.setRoles(
					Set.of(
						new Role()
							.setName(Role.Name.user)
							.setApplication(
								new Application().setName("any")
							)
					)
				)
		);

		assertEquals(
			"Joe",
			jwtService.parse(token).getBody().get("userName")
		);
	}
}