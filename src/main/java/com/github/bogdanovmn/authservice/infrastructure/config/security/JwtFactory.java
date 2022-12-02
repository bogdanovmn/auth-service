package com.github.bogdanovmn.authservice.infrastructure.config.security;

import com.github.bogdanovmn.authservice.common.FileResource;
import com.github.bogdanovmn.authservice.common.RSAKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFactory {
	private PrivateKey privateKey;
	private PublicKey publicKey;

	@Value("${jwt.private-key-path:}")
	private final String privateKeyPath;

	@Value("${jwt.public-key-path:}")
	private final String publicKeyPath;

	@Value("${jwt.ttl-in-minutes:30}")
	private final long tokenTtlInMinutes;

	@Value("${jwt.refresh-token.ttl-in-hours:48}")
	private final long refreshTokenTtlInHours;

	@PostConstruct
	public void loadKeys() throws IOException {
		log.info("JWT private key loading: {}", privateKeyPath);
		privateKey = RSAKey.ofDER(
			new FileResource(privateKeyPath).content()
		).asPrivateKey();

		log.info("JWT public key loading: {}", publicKeyPath);
		publicKey = RSAKey.ofDER(
			new FileResource(publicKeyPath).content()
		).asPublicKey();
	}

	public String createToken(Map<String, Object> claims) {
		Date expiresAt = new Date(System.currentTimeMillis() + tokenTtlInMinutes * 60_000L);

		JwtBuilder token = Jwts.builder()
			.setClaims(claims)
			.signWith(privateKey)
			.setIssuedAt(new Date())
			.setExpiration(expiresAt)
			.setId(UUID.randomUUID().toString());

		log.info("JWS token has been created: claims={}, expires={}", claims, expiresAt);

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

		log.info("JWS refresh token has been created: id={}, claims={}, expires={}", tokenId, claims, expiresAt);

		return token.compact();
	}

	public Jws<Claims> checkSignatureAndReturnClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(token);
	}

	public Date refreshTokenExpiresAt() {
		return new Date(System.currentTimeMillis() + refreshTokenTtlInHours * 3600_000);
	}
}
