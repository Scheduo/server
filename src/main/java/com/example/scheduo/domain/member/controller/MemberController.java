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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Tag(name = "Member", description = "멤버 관련 API")
public class MemberController {
	private final MemberService memberService;

	@GetMapping("/me")
	@Operation(summary = "프로필 조회", description = "사용자의 프로필을 조회합니다.")
	public ApiResponse<MemberResponseDto.MemberInfo> retrieveMember(@RequestMember Member member) {
		Long memberId = member.getId();
		MemberResponseDto.MemberInfo data = memberService.getMyProfile(memberId);
		return ApiResponse.onSuccess(data);
	}

	@PatchMapping("/me")
	@Operation(summary = "프로필 수정", description = "사용자의 정보를 수정합니다.")
	public ApiResponse<MemberResponseDto.MemberInfo> modifiedMember(@RequestMember Member member, @RequestBody MemberRequestDto.EditInfo body) {
		Long memberId = member.getId();
		MemberResponseDto.MemberInfo data = memberService.editMyProfile(memberId, body);
		return ApiResponse.onSuccess(data);
	}

	@GetMapping("/search")
	@Operation(summary = "사용자 찾기", description = "사용자의 이메일 prefix로 사용자를 검색합니다.")
	public ApiResponse<MemberResponseDto.MemberList> searchMember(String email) {
		MemberResponseDto.MemberList data = memberService.searchByEmail(email);
		return ApiResponse.onSuccess(data);
	}
}
