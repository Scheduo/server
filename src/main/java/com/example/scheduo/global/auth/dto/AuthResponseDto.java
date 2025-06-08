package com.example.scheduo.global.auth.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthResponseDto {
	@Getter
	@Builder
	public static class Token {
		private final String accessToken;
		private final String refreshToken;

		public static Token of(String accessToken, String refreshToken) {
			return Token.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();
		}
	}
}
