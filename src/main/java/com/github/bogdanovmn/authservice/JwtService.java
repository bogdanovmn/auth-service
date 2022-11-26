package com.github.bogdanovmn.authservice;

import com.github.bogdanovmn.authservice.common.FileResource;
import com.github.bogdanovmn.authservice.common.RSAKey;
import com.github.bogdanovmn.authservice.model.Account;
import com.github.bogdanovmn.authservice.model.RefreshToken;
import com.github.bogdanovmn.authservice.model.RefreshTokenRepository;
import com.github.bogdanovmn.authservice.model.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
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

	@Value("${jwt.ttl-in-minutes:10}")
	private final long tokenTtlInMinutes;

	@Value("${jwt.refresh-token.ttl-in-hours:48}")
	private final long refreshTokenTtlInHours;

	private final RefreshTokenRepository refreshTokenRepository;

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
		Map<String, Object> claims = Map.of(
			"roles",    account.getRoles().stream().map(Role::toString).collect(Collectors.toSet()),
			"userId",   account.getId(),
			"userName", account.getName()
		);
		Date expiresAt = new Date(System.currentTimeMillis() + tokenTtlInMinutes * 60_000L);

		JwtBuilder token = Jwts.builder()
			.setClaims(claims)
			.signWith(privateKey)
			.setIssuedAt(new Date())
			.setExpiration(expiresAt);

		log.info("JWS token has been created for {}: claims: {}, expires: {}", account, claims, expiresAt);

		return token.compact();
	}

	@Transactional
	public String createRefreshToken(Account account) {
		log.info("Creating refresh token for {}", account);
		Optional<RefreshToken> previousRefreshToken = refreshTokenRepository.getByAccount(account);
		previousRefreshToken.ifPresent(
			rt -> {
				refreshTokenRepository.delete(rt);
				refreshTokenRepository.flush();
				log.info("Previous refresh token has been deleted: {}", rt);
			}
		);

		Date expiresAt = new Date(System.currentTimeMillis() + refreshTokenTtlInHours * 3600_000);
		RefreshToken refreshToken = refreshTokenRepository.save(
			new RefreshToken()
				.setAccount(account)
				.setExpiresAt(expiresAt)
		);

		log.info("A new refresh token has been created: {}", refreshToken);

		return Jwts.builder()
			.setId(refreshToken.getId().toString())
			.claim("userId", account.getId())
			.signWith(privateKey)
			.setIssuedAt(new Date())
			.setExpiration(expiresAt)
		.compact();
	}

	public Jws<Claims> parse(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(token);
	}
}
