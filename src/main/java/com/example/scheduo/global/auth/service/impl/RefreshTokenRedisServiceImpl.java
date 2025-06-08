package com.example.scheduo.global.auth.service.impl;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.example.scheduo.global.auth.service.RefreshTokenService;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisServiceImpl implements RefreshTokenService {

	private final RedisTemplate<String, String> redisTemplate;

	@Override
	public void saveRefreshToken(Long memberId, String deviceUUID, String refreshToken, long ttlMs) {
		String key = "refresh:" + memberId + ":" + deviceUUID;
		redisTemplate.opsForValue().set(key, refreshToken, ttlMs, TimeUnit.MILLISECONDS);
	}

	@Override
	public Optional<String> getRefreshToken(Long memberId, String deviceUUID) {
		String key = "refresh:" + memberId + ":" + deviceUUID;
		return Optional.ofNullable(redisTemplate.opsForValue().get(key));
	}

	@Override
	public void deleteRefreshToken(Long memberId, String deviceUUID, String refreshToken) {
		String key = "refresh:" + memberId + ":" + deviceUUID;
		String value = redisTemplate.opsForValue().get(key);
		if (value == null)
			throw new ApiException(ResponseStatus.EXPIRED_REFRESH_TOKEN);

		if (!value.equals(refreshToken))
			throw new ApiException(ResponseStatus.EXPIRED_REFRESH_TOKEN);

		redisTemplate.delete(key);
	}
}
