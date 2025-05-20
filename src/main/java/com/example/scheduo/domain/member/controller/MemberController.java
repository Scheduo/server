package com.example.scheduo.domain.member.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

	@GetMapping("/me")
	public ApiResponse<MemberResponseDto.GetProfile> retrieveMember() {
		MemberResponseDto.GetProfile data = memberService.getMyProfile();
		return ApiResponse.onSuccess(data);
	}

	@PatchMapping("/me")
	public ApiResponse<MemberResponseDto.GetProfile> modifiedMember(@RequestBody MemberRequestDto.EditInfo body) {
		MemberResponseDto.GetProfile data = memberService.editMyProfile(body);
		return ApiResponse.onSuccess(data);
	}

	@GetMapping("/search")
	public ApiResponse<List<MemberResponseDto.GetProfile>> searchMember(String query) {
		List<MemberResponseDto.GetProfile> data = memberService.searchByEmail(query);
		return ApiResponse.onSuccess(data);
	}
}
