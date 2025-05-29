package com.example.scheduo.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;
import com.example.scheduo.domain.member.service.MemberService;
import com.example.scheduo.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
	private final MemberService memberService;

	// TODO: tempId 대신 SecurityContextHolder 이용하기
	@GetMapping("/me")
	public ApiResponse<MemberResponseDto.MemberInfo> retrieveMember(@RequestParam Long tempId) {
		MemberResponseDto.MemberInfo data = memberService.getMyProfile(tempId);
		return ApiResponse.onSuccess(data);
	}

	// TODO: tempId 대신 SecurityContextHolder 이용하기
	@PatchMapping("/me")
	public ApiResponse<MemberResponseDto.MemberInfo> modifiedMember(@RequestParam Long tempId, @RequestBody MemberRequestDto.EditInfo body) {
		MemberResponseDto.MemberInfo data = memberService.editMyProfile(tempId, body);
		return ApiResponse.onSuccess(data);
	}

	@GetMapping("/search")
	public ApiResponse<MemberResponseDto.MemberList> searchMember(String email) {
		MemberResponseDto.MemberList data = memberService.searchByEmail(email);
		return ApiResponse.onSuccess(data);
	}
}
