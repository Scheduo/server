package com.example.scheduo.domain.member.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.member.dto.NotificationResponseDto;
import com.example.scheduo.domain.member.entity.Notification;
import com.example.scheduo.domain.member.repository.NotificationRepository;
import com.example.scheduo.domain.member.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;

	@Override
	@Transactional(readOnly = true)
	public NotificationResponseDto.GetNotifications findAllByMemberId(Long memberId) {
		List<Notification> notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
		List<NotificationResponseDto.GetNotification> notificationDtos = notifications.stream()
			.map(NotificationResponseDto.GetNotification::from)
			.collect(Collectors.toList());

		return NotificationResponseDto.GetNotifications.from(notificationDtos);
	}
}