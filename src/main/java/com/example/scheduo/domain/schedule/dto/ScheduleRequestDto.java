package com.example.scheduo.domain.schedule.dto;

import com.example.scheduo.domain.schedule.entity.NotificationTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ScheduleRequestDto {
	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Create {
		private String title; // 일정 제목
		private boolean allDay; // 종일 일정 여부
		private String startDate; // 시작 날짜 (yyyy-mm-dd)
		private String endDate; // 종료 날짜 (yyyy-mm-dd)
		private String startTime; // 시작 시간 (hh:mm)
		private String endTime; // 종료 시간 (hh:mm)
		private String location; // 장소
		private String category; // 일정 카테고리
		private String memo; // 메모
		private NotificationTime notificationTime; // 알림 시간 (예: THIRTY_MINUTES_BEFORE)
		private Recurrence recurrence; // 반복 일정 정보
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Recurrence {
		private String frequency; // 반복 규칙 (예: DAILY, WEEKLY, MONTHLY, YEARLY)
		private String recurrenceEndDate; // 반복 종료 날짜 (yyyy-mm-dd)
	}
}