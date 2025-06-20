package com.example.scheduo.domain.member.service;

import java.util.Map;

import com.example.scheduo.domain.member.dto.NotificationResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.NotificationType;

public interface NotificationService {
	NotificationResponseDto.NoticeList findAllByMemberId(Long memberId);

	void createNotification(Member member, NotificationType notificationType, Map<String, Object> data);

	void readNotification(Long notificationId, Long memberId);
}
