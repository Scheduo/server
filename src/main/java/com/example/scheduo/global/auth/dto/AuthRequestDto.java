package com.example.scheduo.global.auth.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthRequestDto {
	@Getter
	@Builder
	public static class RefreshToken {
		private final String refreshToken;
	}
}
