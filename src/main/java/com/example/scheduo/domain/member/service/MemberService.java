package com.example.scheduo.domain.member.service;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;

public interface MemberService {
	MemberResponseDto.GetProfile getMyProfile(Long memberId);
	MemberResponseDto.GetProfile editMyProfile(Long memberId, MemberRequestDto.EditInfo dto);
	MemberResponseDto.SearchProfiles searchByEmail(String email);
}
