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
	public static class SchedulesOnMonth {
		private Long calendarId;
		List<ScheduleOnMonth> schedules;

		public static SchedulesOnMonth from(Long calendarId,
			List<com.example.scheduo.domain.schedule.entity.Schedule> schedules) {
			List<ScheduleOnMonth> scheduleList = schedules.stream()
				.map(schedule -> new ScheduleOnMonth(
					schedule.getId(),
					schedule.getTitle(),
					schedule.getStartDate(),
					schedule.getEndDate(),
					new Category(schedule.getCategory().getName(), schedule.getCategory().getColor())
				))
				.toList();
			return new SchedulesOnMonth(calendarId, scheduleList);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SchedulesOnDate {
		List<ScheduleOnMonth> schedules;
	}


		@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class ScheduleOnMonth {
		private Long id;
		private String title;
		private LocalDate startDate;
		private LocalDate endDate;
		private Category category;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class ScheduleOnDate {
		private Long id;
		private String title;
		private LocalDate startTime;
		private LocalDate endTime;
		private boolean isAllDay;
		private Category category;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class Category {
		private String name;
		private Color color;
	}
}
