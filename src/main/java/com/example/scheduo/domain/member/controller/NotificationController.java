package com.example.scheduo.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.member.dto.NotificationResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.service.NotificationService;
import com.example.scheduo.global.auth.annotation.RequestMember;
import com.example.scheduo.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ApiResponse<NotificationResponseDto.NoticeList> getNotifications(@RequestMember Member member) {
		Long memberId = member.getId();
		NotificationResponseDto.NoticeList notifications = notificationService.findAllByMemberId(memberId);
		return ApiResponse.onSuccess(notifications);
	}
}