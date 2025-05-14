package com.example.scheduo.global.config.security.provider;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider implements InitializingBean {

	@Value("${jwt.token.secret}")
	private String secret;
	private static SecretKey secretKey;
	private static final Long expireAccessMs = 1000L * 60 * 15; // 15분
	private static final Long expireRefreshMs = 1000L * 60 * 60 * 24 * 7; // 7일

	@Override
	public void afterPropertiesSet() throws Exception {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		secretKey = Keys.hmacShaKeyFor(keyBytes);
	}

	public String createAccessToken(Long memberId) {
		return Jwts.builder()
			.claim("memberId", memberId)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + expireAccessMs))
			.signWith(secretKey)
			.compact();
	}

	public String createRefreshToken(Long memberId) {
		return Jwts.builder()
			.claim("memberId", memberId)
			.setIssuedAt(new Date(System.currentTimeMillis()))
			.setExpiration(new Date(System.currentTimeMillis() + expireRefreshMs))
			.signWith(secretKey)
			.compact();
	}

}
