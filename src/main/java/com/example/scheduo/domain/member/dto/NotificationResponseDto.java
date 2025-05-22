package com.example.scheduo.domain.member.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.example.scheduo.domain.member.entity.Notification;
import com.example.scheduo.domain.member.entity.NotificationType;

import lombok.Builder;
import lombok.Getter;

public class NotificationResponseDto {
	@Getter
	@Builder
	public static class GetNotification {
		private Long id;
		private NotificationType type;
		private String title;
		private Map<String, Object> data;
		private Boolean isRead;
		private LocalDateTime createdAt;

		public static GetNotification from(Notification notification) {
			return GetNotification.builder()
				.id(notification.getId())
				.type(notification.getNotificationType())
				.title(notification.getTitle())
				.data(notification.getData())
				.isRead(false)
				.createdAt(notification.getCreatedAt())
				.build();
		}
	}

	@Getter
	@Builder
	public static class GetNotifications {
		private List<GetNotification> notifications;

		public static GetNotifications from(List<GetNotification> notifications) {
			return GetNotifications.builder()
				.notifications(notifications)
				.build();
		}
	}
}
