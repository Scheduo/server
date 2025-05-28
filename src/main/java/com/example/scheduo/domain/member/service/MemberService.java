package com.example.scheduo.domain.member.service;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;

public interface MemberService {
	MemberResponseDto.GetProfile getMyProfile(Long memberId);

	MemberResponseDto.GetProfile editMyProfile(Long memberId, MemberRequestDto.EditInfo dto);

	MemberResponseDto.SearchProfiles searchByEmail(String email);

	Member findOrCreateMember(String email, String nickname, SocialType socialType);
}
