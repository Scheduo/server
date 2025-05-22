package com.example.scheduo.domain.member.dto;

import com.example.scheduo.domain.member.entity.Member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MemberResponseDto {
	@Getter
	@RequiredArgsConstructor
	public static class GetProfile {
		private final Long id;
		private final String email;
		private final String nickname;

		public static GetProfile from(Member member) {
			return new GetProfile(
				member.getId(),
				member.getEmail(),
				member.getNickname()
			);
		}
	}
}
