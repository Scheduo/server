package com.example.scheduo.domain.member.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.member.dto.NotificationResponseDto;
import com.example.scheduo.domain.member.entity.Notification;
import com.example.scheduo.domain.member.service.NotificationService;
import com.example.scheduo.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

	private final NotificationService notificationService;

	@GetMapping
	public ApiResponse<NotificationResponseDto.GetNotifications> getNotifications(Long memberId) {
		NotificationResponseDto.GetNotifications notifications = notificationService.findAllByMemberId(memberId);
		return ApiResponse.onSuccess(notifications);
	}
}