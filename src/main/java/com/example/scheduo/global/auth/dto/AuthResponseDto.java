package com.example.scheduo.global.auth.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthResponseDto {
	@Getter
	@Builder
	public static class Token {
		private final String accessToken;
		private final String refreshToken;
	}
}
