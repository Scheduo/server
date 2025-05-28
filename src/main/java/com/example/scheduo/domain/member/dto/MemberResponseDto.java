package com.example.scheduo.domain.member.dto;

import java.util.List;

import com.example.scheduo.domain.member.entity.Member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MemberResponseDto {
	@Getter
	@RequiredArgsConstructor
	public static class MemberInfo {
		private final Long id;
		private final String email;
		private final String nickname;

		public static MemberInfo from(Member member) {
			return new MemberInfo(
				member.getId(),
				member.getEmail(),
				member.getNickname()
			);
		}
	}

	@Getter
	@RequiredArgsConstructor
	public static class MemberList {
		private final List<MemberInfo> users;

		public static MemberList from(List<Member> members) {
			List<MemberInfo> profiles = members.stream()
				.map(MemberInfo::from)
				.toList();
			return new MemberList(profiles);
		}
	}
}
