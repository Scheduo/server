package com.example.scheduo.global.auth.service.impl;

import org.springframework.stereotype.Service;

import com.example.scheduo.global.auth.dto.AuthResponseDto;
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
			throw new ApiException(ResponseStatus.EXPIRED_REFRESH_TOKEN);

		refreshTokenService.deleteRefreshToken(memberIdByRT, deviceUUID, refreshToken);
	}

	@Override
	public AuthResponseDto.Token rotateToken(Long memberIdByAT, String refreshToken) {
		Long memberIdByRT = jwtProvider.getMemberIdFromToken(refreshToken);
		String deviceUUID = jwtProvider.getDeviceUUIDFromToken(refreshToken);

		// AccessToken과 RefreshToken의 memberId 일치 여부 확인
		if (!memberIdByRT.equals(memberIdByAT))
			throw new ApiException(ResponseStatus.REFRESH_TOKEN_MEMBER_MISMATCH);

		// Redis에 클라이언트 RefreshToken 정보가 존재하는지 확인
		String storedRefreshToken = refreshTokenService.getRefreshToken(memberIdByRT, deviceUUID)
				.orElseThrow(() -> new ApiException(ResponseStatus.EXPIRED_REFRESH_TOKEN));

		// 실제 DB 값과 요청 RefreshToken 값 비교
		if (!storedRefreshToken.equals(refreshToken)) {
			throw new ApiException(ResponseStatus.EXPIRED_REFRESH_TOKEN);
		}

		String newAccessToken = jwtProvider.createAccessToken(memberIdByRT);
		String newRefreshToken = jwtProvider.createRefreshToken(memberIdByRT, deviceUUID);

		refreshTokenService.saveRefreshToken(memberIdByRT, deviceUUID, newRefreshToken, JwtProvider.EXPIRE_REFRESH_MS);

		return AuthResponseDto.Token.of(newAccessToken, newRefreshToken);
	}
}
