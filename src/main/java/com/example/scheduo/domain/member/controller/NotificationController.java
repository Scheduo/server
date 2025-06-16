package com.example.scheduo.domain.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.member.dto.NotificationResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.service.NotificationService;
import com.example.scheduo.global.auth.annotation.RequestMember;
import com.example.scheduo.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	@Operation(summary = "알림 조회", description = "사용자의 모든 알림을 조회합니다.")
	public ApiResponse<NotificationResponseDto.NoticeList> getNotifications(@RequestMember Member member) {
		Long memberId = member.getId();
		NotificationResponseDto.NoticeList notifications = notificationService.findAllByMemberId(memberId);
		return ApiResponse.onSuccess(notifications);
	}

	@PostMapping("/{notificationId}/read")
	public ApiResponse<?> readNotification(@PathVariable("notificationId") Long notificationId,
		@RequestMember Member member) {
		Long memberId = member.getId();
		notificationService.readNotification(notificationId, memberId);
		return ApiResponse.onSuccess();
	}

}