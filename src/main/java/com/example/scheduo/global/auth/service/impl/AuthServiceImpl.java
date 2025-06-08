package com.example.scheduo.global.auth.service.impl;

import org.springframework.stereotype.Service;

import com.example.scheduo.global.auth.service.AuthService;
import com.example.scheduo.global.auth.service.RefreshTokenService;
import com.example.scheduo.global.config.security.provider.JwtProvider;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	@Override
	public void logout(Long memberIdByAT, String refreshToken) {
		// Refresh Token 검증
		if (!jwtProvider.validateToken(refreshToken))
			throw new ApiException(ResponseStatus.REFRESH_TOKEN_INVALID);

		Long memberIdByRT = jwtProvider.getMemberIdFromToken(refreshToken);
		String deviceUUID = jwtProvider.getDeviceUUIDFromToken(refreshToken);

		// AccessToken과 RefreshToken의 memberId 일치 여부 확인
		if (!memberIdByAT.equals(memberIdByRT))
			throw new ApiException(ResponseStatus.REFRESH_TOKEN_MEMBER_MISMATCH);

		if (refreshTokenService.getRefreshToken(memberIdByRT, deviceUUID).isEmpty())
			throw new ApiException(ResponseStatus.ALREADY_LOGGED_OUT_REFRESH_TOKEN);

		refreshTokenService.deleteRefreshToken(memberIdByRT, deviceUUID, refreshToken);
	}
}
