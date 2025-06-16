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
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

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

	@Override
	@Transactional
	public void readNotification(Long notificationId, Long memberId) {
		Notification notification = notificationRepository.findById(notificationId)
			.orElseThrow(() -> new ApiException(ResponseStatus.NOTIFICATION_NOT_FOUND));
		System.out.println(notification.getMember().getId());
		System.out.println(memberId);
		if (!notification.validateOwner(memberId)) {
			throw new ApiException(ResponseStatus.NOTIFICATION_NOW_OWNER);
		}

		notificationRepository.delete(notification);
	}
}