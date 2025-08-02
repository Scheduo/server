package com.example.scheduo.domain.schedule.dto;

import java.time.LocalDate;
import java.util.List;

import com.example.scheduo.domain.schedule.entity.Color;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ScheduleResponseDto {
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SchedulesByMonthly {
		private Long calendarId;
		List<Schedule> schedules;

		public static SchedulesByMonthly from(Long calendarId,
			List<com.example.scheduo.domain.schedule.entity.Schedule> schedules) {
			List<Schedule> scheduleList = schedules.stream()
				.map(schedule -> new Schedule(
					schedule.getId(),
					schedule.getTitle(),
					schedule.getStartDate(),
					schedule.getEndDate(),
					new Category(schedule.getCategory().getName(), schedule.getCategory().getColor())
				))
				.toList();
			return new SchedulesByMonthly(calendarId, scheduleList);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Schedule {
		private Long id;
		private String title;
		private LocalDate startDate;
		private LocalDate endDate;
		private Category category;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Category {
		private String name;
		private Color color;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class ScheduleInfo {
		private Long id;
		private String title;
		private boolean isAllDay;
		private LocalDate startDate;
		private LocalDate endDate;
		private String startTime;
		private String endTime;
		private String location;
		private String category;
		private String memo;
		private String notificationTime;
		private String frequency;
		private LocalDate recurrenceEndDate;

		public static ScheduleInfo from(com.example.scheduo.domain.schedule.entity.Schedule schedule) {
			String frequency = schedule.getRecurrence() != null
				? schedule.getRecurrence().getFrequency()
				: null;
			return new ScheduleInfo(
				schedule.getId(),
				schedule.getTitle(),
				schedule.isAllDay(),
				schedule.getStartDate(),
				schedule.getEndDate(),
				schedule.getStartTime().toString(),
				schedule.getEndTime().toString(),
				schedule.getLocation(),
				schedule.getCategory().getName(),
				schedule.getMemo(),
				schedule.getNotificationTime() != null ? schedule.getNotificationTime().name() : null,
				frequency,
				schedule.getRecurrence() != null ? schedule.getRecurrence().getRecurrenceEndDate() : null
			);
		}
	}
}
