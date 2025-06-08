package com.example.scheduo.global.auth.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthRequestDto {
	@Getter
	@Builder
	public static class Logout {
		private final String refreshToken;
	}
}
