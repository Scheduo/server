package com.example.scheduo.global.config.security.provider;

import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtProvider implements InitializingBean {

	@Value("${jwt.token.secret}")
	private String secret;
	private static SecretKey secretKey;
	private static final Long EXPIRE_ACCESS_MS = 1000L * 60 * 15; // 15분
	public static final Long EXPIRE_REFRESH_MS = 1000L * 60 * 60 * 24 * 7; // 7일

	@Override
	public void afterPropertiesSet() throws Exception {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String createAccessToken(Long memberId) {
		return Jwts.builder()
			.claim("memberId", memberId)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + EXPIRE_ACCESS_MS))
			.signWith(secretKey)
			.compact();
	}

	public String createRefreshToken(Long memberId, String deviceUUID) {
		long now = System.currentTimeMillis();
		Date issuedAt = new Date(now);
		Date expiresAt = new Date(now + EXPIRE_REFRESH_MS);

		return Jwts.builder()
			.claim("memberId", memberId)
			.claim("deviceUUID", deviceUUID)
			.setIssuedAt(issuedAt)
			.setExpiration(expiresAt)
			.signWith(secretKey)
			.compact();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
				.setSigningKey(secretKey)
				.build()
				.parseClaimsJws(token);
			return true;
		} catch (Exception e) {
			log.error("올바른 토큰이 아닙니다.");
			return false;
		}
	}

	public Authentication getAuthentication(String token) {
		Long memberId = getMemberIdFromToken(token);
		return new UsernamePasswordAuthenticationToken(memberId, "", List.of());
	}

	public Long getMemberIdFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
		return claims.get("memberId", Long.class);
	}

	public String getDeviceUUIDFromToken(String token) {
		Claims claims = Jwts.parserBuilder()
			.setSigningKey(secretKey)
			.build()
			.parseClaimsJws(token)
			.getBody();
		return claims.get("deviceUUID", String.class);
	}

}
