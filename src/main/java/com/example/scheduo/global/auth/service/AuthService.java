package com.example.scheduo.global.auth.service;

public interface AuthService {
	void logout(Long memberId, String refreshToken);
}
