package com.example.scheduo.domain.member.service;

import java.util.List;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;

public interface MemberService {
	MemberResponseDto.GetProfile getMyProfile(Long memberId);
	MemberResponseDto.GetProfile editMyProfile(Long memberId, MemberRequestDto.EditInfo dto);
	List<MemberResponseDto.GetProfile> searchByEmail(String email);
}
