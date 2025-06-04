package com.example.scheduo.domain.member.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.scheduo.domain.member.entity.Notification;
import com.example.scheduo.domain.member.entity.NotificationType;

import lombok.Builder;
import lombok.Getter;

public class NotificationResponseDto {
	@Getter
	@Builder
	public static class NoticeInfo {
		private Long id;
		private NotificationType type;
		private String title;
		private Map<String, Object> data;
		private Boolean isRead;
		private LocalDateTime createdAt;

		public static NoticeInfo from(Notification notification) {
			return NoticeInfo.builder()
				.id(notification.getId())
				.type(notification.getNotificationType())
				.title(notification.getMessage())
				.data(notification.getData())
				.isRead(false)
				.createdAt(notification.getCreatedAt())
				.build();
		}
	}

	@Getter
	@Builder
	public static class NoticeList {
		private List<NoticeInfo> notifications;

		public static NoticeList from(List<Notification> notifications) {
			List<NotificationResponseDto.NoticeInfo> noticeList = notifications.stream()
				.map(NotificationResponseDto.NoticeInfo::from)
				.collect(Collectors.toList());

			return NoticeList.builder()
				.notifications(noticeList)
				.build();
		}
	}
}
