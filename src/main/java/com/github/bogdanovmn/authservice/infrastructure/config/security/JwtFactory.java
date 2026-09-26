package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.FileResource;
import com.github.bogdanovmn.authservice.common.RSAKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@EnableConfigurationProperties(JwtProperties.class)
@RequiredArgsConstructor
@Slf4j
public class JwtFactory {
	private final JwtProperties properties;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	@PostConstruct
	public void loadKeys() throws IOException {
		log.info("JWT private key loading: {}", properties.getPrivateKeyPath());
		privateKey = RSAKey.ofDER(
			new FileResource(properties.getPrivateKeyPath()).content()
		).asPrivateKey();

		log.info("JWT public key loading: {}", properties.getPublicKeyPath());
		publicKey = RSAKey.ofDER(
			new FileResource(properties.getPublicKeyPath()).content()
		).asPublicKey();
	}

	public String createToken(Map<String, Object> claims) {
		Date expiresAt = new Date(System.currentTimeMillis() + properties.getTtlInMinutes() * 60_000L);
		String tokenId = UUID.randomUUID().toString();

		JwtBuilder token = Jwts.builder()
			.setClaims(claims)
			.signWith(privateKey)
			.setIssuedAt(new Date())
			.setExpiration(expiresAt)
			.setId(tokenId);

		log.info("JWS token has been created: id={}, expires={}", tokenId, expiresAt);

		return token.compact();
	}

	public String createRefreshToken(UUID tokenId, Map<String, Object> claims) {
		Date expiresAt = refreshTokenExpiresAt();
		JwtBuilder token = Jwts.builder()
			.setClaims(claims)
			.signWith(privateKey)
			.setIssuedAt(new Date())
			.setExpiration(expiresAt)
			.setId(tokenId.toString());

		log.info("JWS refresh token has been created: id={}, expires={}", tokenId, expiresAt);

		return token.compact();
	}

	public Jws<Claims> checkSignatureAndReturnClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(token);
	}

	public Date refreshTokenExpiresAt() {
		return new Date(
			System.currentTimeMillis()
				+ properties.getRefreshToken().getTtlInHours() * 3600_000
		);
	}
}
