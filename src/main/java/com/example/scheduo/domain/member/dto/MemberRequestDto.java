package com.example.scheduo.domain.member.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MemberRequestDto {
	@Getter
	@RequiredArgsConstructor
	public static class EditInfo {
		private final String nickname;
	}
}
