package com.github.bogdanovmn.authservice;

import com.github.bogdanovmn.authservice.common.FileResource;
import com.github.bogdanovmn.authservice.common.RSAKey;
import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {
	private PrivateKey privateKey;
	private PublicKey publicKey;

	@Value("${jwt.private-key-path:}")
	private final String privateKeyPath;

	@Value("${jwt.public-key-path:}")
	private final String publicKeyPath;

	@PostConstruct
	public void loadKeys() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		log.info("JWT private key loading: {}", privateKeyPath);
		privateKey = RSAKey.ofDER(
			new FileResource(privateKeyPath).content()
		).asPrivateKey();

		log.info("JWT public key loading: {}", publicKeyPath);
		publicKey = RSAKey.ofDER(
			new FileResource(publicKeyPath).content()
		).asPublicKey();
	}

	public String createToken(Account account) {
		return Jwts.builder()
			.claim(
				"roles",
				account.getRoles().stream()
					.map(Role::toString)
					.collect(Collectors.toSet())
			)
			.claim("userId", account.getId())
			.claim("userName", account.getName())
			.signWith(privateKey)
			.setExpiration(
				Date.from(
					LocalDateTime.now().plus(30, ChronoUnit.MINUTES).toInstant(ZoneOffset.UTC)
				)
			).compact();
	}

	public String createRefreshToken(Account account) {
		return null;
	}

	public Jws<Claims> parse(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(token);
	}
}
