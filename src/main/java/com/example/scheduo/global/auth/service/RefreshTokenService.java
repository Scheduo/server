package com.example.scheduo.global.auth.service;

import java.util.Optional;

public interface RefreshTokenService {
	void saveRefreshToken(Long memberId, String deviceUUID, String refreshToken, long ttlMs);

	Optional<String> getRefreshToken(Long memberId, String deviceUUID);

	void deleteRefreshToken(Long memberId, String deviceUUID, String refreshToken);
}
