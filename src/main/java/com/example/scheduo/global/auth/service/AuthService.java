package com.example.scheduo.global.auth.service;

import com.example.scheduo.global.auth.dto.AuthResponseDto;

public interface AuthService {
	void logout(Long memberId, String refreshToken);
	AuthResponseDto.Token rotateToken(Long memberId, String refreshToken);
}
