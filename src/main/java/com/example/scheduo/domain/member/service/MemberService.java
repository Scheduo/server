package com.example.scheduo.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;

@Service
public interface MemberService {
	MemberResponseDto.GetProfile getMyProfile();
	MemberResponseDto.GetProfile editMyProfile(MemberRequestDto.EditInfo dto);
	List<MemberResponseDto.GetProfile> searchByEmail(String email);
}
