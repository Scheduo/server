package com.example.scheduo.domain.member.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MemberResponseDto {
	@Getter
	@RequiredArgsConstructor
	public static class GetProfile {
		private final Long id;
		private final String email;
		private final String nickname;
	}
}
