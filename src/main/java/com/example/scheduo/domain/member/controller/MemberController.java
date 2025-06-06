package com.example.scheduo.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.service.MemberService;
import com.example.scheduo.global.auth.annotation.RequestMember;
import com.example.scheduo.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	@GetMapping("/me")
	public ApiResponse<MemberResponseDto.MemberInfo> retrieveMember(@RequestMember Member member) {
		Long memberId = member.getId();
		MemberResponseDto.MemberInfo data = memberService.getMyProfile(memberId);
		return ApiResponse.onSuccess(data);
	}

	@PatchMapping("/me")
	public ApiResponse<MemberResponseDto.MemberInfo> modifiedMember(@RequestMember Member member, @RequestBody MemberRequestDto.EditInfo body) {
		Long memberId = member.getId();
		MemberResponseDto.MemberInfo data = memberService.editMyProfile(memberId, body);
		return ApiResponse.onSuccess(data);
	}

	@GetMapping("/search")
	public ApiResponse<MemberResponseDto.MemberList> searchMember(String email) {
		MemberResponseDto.MemberList data = memberService.searchByEmail(email);
		return ApiResponse.onSuccess(data);
	}
}
