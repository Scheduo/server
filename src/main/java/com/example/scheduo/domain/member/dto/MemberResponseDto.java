package com.example.scheduo.domain.member.dto;

import java.util.List;

import com.example.scheduo.domain.member.entity.Member;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class MemberResponseDto {
	@Getter
	@Builder
	public static class MemberInfo {
		private Long id;
		private String email;
		private String nickname;

		public static MemberInfo from(Member member) {
			return MemberInfo.builder()
				.id(member.getId())
				.email(member.getEmail())
				.nickname(member.getNickname())
				.build();
		}
	}

	@Getter
	@Builder
	public static class MemberList {
		private List<MemberInfo> users;

		public static MemberList from(List<Member> members) {
			List<MemberInfo> profiles = members.stream()
				.map(MemberInfo::from)
				.toList();

			return MemberList.builder()
				.users(profiles)
				.build();
		}
	}
}
