package com.example.scheduo.domain.schedule.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.scheduo.domain.schedule.entity.NotificationTime;

import jakarta.validation.constraints.Pattern;
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
		@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-mm-dd이어야 합니다.")
		private String startDate; // 시작 날짜 (yyyy-mm-dd)
		@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-mm-dd이어야 합니다.")
		private String endDate; // 종료 날짜 (yyyy-mm-dd)
		@Pattern(regexp = "\\d{2}:\\d{2}", message = "시간 형식은 hh:mm이어야 합니다.")
		private String startTime; // 시작 시간 (hh:mm)
		@Pattern(regexp = "\\d{2}:\\d{2}", message = "시간 형식은 hh:mm이어야 합니다.")
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
		@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-mm-dd이어야 합니다.")
		private String recurrenceEndDate; // 반복 종료 날짜 (yyyy-mm-dd)
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Update {
		private String title; // 일정 제목
		private boolean allDay; // 종일 일정 여부
		@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-mm-dd이어야 합니다.")
		private String startDate; // 시작 날짜 (yyyy-mm-dd)
		@Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "날짜 형식은 yyyy-mm-dd이어야 합니다.")
		private String endDate; // 종료 날짜 (yyyy-mm-dd)
		@Pattern(regexp = "\\d{2}:\\d{2}", message = "시간 형식은 hh:mm이어야 합니다.")
		private String startTime; // 시작 시간 (hh:mm)
		@Pattern(regexp = "\\d{2}:\\d{2}", message = "시간 형식은 hh:mm이어야 합니다.")
		private String endTime; // 종료 시간 (hh:mm)
		private String location; // 장소
		private String category; // 일정 카테고리
		private String memo; // 메모
		private NotificationTime notificationTime; // 알림 시간 (예: THIRTY_MINUTES_BEFORE)
		private Recurrence recurrence; // 반복 일정 정보
		private Scope scope;
	}

	@Getter
	public enum Scope {
		ALL, // 모든 일정
		ONLY_THIS, // 현재 일정만
		THIS_AND_FUTURE // 현재 일정과 이후 일정
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Share {
		private Long targetCalendarId;
		private List<ScheduleTime> schedules;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ScheduleTime {
		private LocalDateTime startDateTime;
		private LocalDateTime endDateTime;
	}
}