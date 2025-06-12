package com.example.scheduo.domain.member.dto;

import java.time.LocalDateTime;
import java.util.HashMap;
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
		private String message;
		private Map<String, Object> data;
		private LocalDateTime createdAt;

		public static NoticeInfo from(Notification notification) {
			Map<String, Object> data = new HashMap<>();
			switch (notification.getNotificationType()) {
				case CALENDAR_INVITATION -> data.put("calendarId", notification.getData().get("calendarId"));
				case SCHEDULE_NOTIFICATION -> data.put("scheduleId", notification.getData().get("scheduleId"));
			}
			return NoticeInfo.builder()
				.id(notification.getId())
				.type(notification.getNotificationType())
				.message(notification.getMessage())
				.data(data)
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
