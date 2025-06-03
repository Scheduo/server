package com.example.scheduo.domain.member.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.member.dto.NotificationResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.Notification;
import com.example.scheduo.domain.member.entity.NotificationType;
import com.example.scheduo.domain.member.repository.NotificationRepository;
import com.example.scheduo.domain.member.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;

	@Override
	public NotificationResponseDto.NoticeList findAllByMemberId(Long memberId) {
		List<Notification> notifications = notificationRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
		return NotificationResponseDto.NoticeList.from(notifications);
	}

	@Override
	@Transactional
	public void createNotification(Member member, NotificationType notificationType, Map<String, Object> data) {
		Notification notification = Notification.builder()
			.member(member)
			.notificationType(notificationType)
			.data(notificationType.createData(data))
			.message(notificationType.createMessage(data))
			.build();

		notificationRepository.save(notification);

	}
}