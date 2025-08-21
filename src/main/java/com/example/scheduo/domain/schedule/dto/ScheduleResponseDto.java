package com.example.scheduo.domain.schedule.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import com.example.scheduo.domain.schedule.entity.Color;
import com.example.scheduo.domain.schedule.entity.Schedule;

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
		List<ScheduleOnDate> schedules;

		public static SchedulesOnDate from(List<Schedule> schedules) {
			List<ScheduleOnDate> scheduleOnDates = schedules.stream()
				.map(ScheduleOnDate::from)
				.collect(Collectors.toList());

			return new SchedulesOnDate(scheduleOnDates);
		}
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
		private String startTime;
		private String endTime;
		private boolean isAllDay;
		private Category category;

		public static ScheduleOnDate from(Schedule schedule) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
			return new ScheduleOnDate(
				schedule.getId(),
				schedule.getTitle(),
				schedule.getStartTime().format(formatter),
				schedule.getEndTime().format(formatter),
				schedule.isAllDay(),
				Category.from(schedule.getCategory().getName(), schedule.getCategory().getColor())
			);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class Category {
		private String name;
		private Color color;

		public static Category from(String name, Color color) {
			return new Category(name, color);
		}
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
		private Recurrence recurrence;

		public static ScheduleInfo from(com.example.scheduo.domain.schedule.entity.Schedule schedule, String date) {
			String frequency = schedule.getRecurrence() != null
				? schedule.getRecurrence().getFrequency()
				: null;

			LocalDate startDate = (date == null || date.isBlank())
				? schedule.getStartDate()
				: LocalDate.parse(date);

			LocalDate endDate;
			if (date == null || date.isBlank()) {
				endDate = schedule.getEndDate();
			} else {
				long daysDiff = ChronoUnit.DAYS.between(schedule.getStartDate(), schedule.getEndDate());
				endDate = startDate.plusDays(daysDiff);
			}

			return new ScheduleInfo(
				schedule.getId(),
				schedule.getTitle(),
				schedule.isAllDay(),
				startDate,
				endDate,
				schedule.getStartTime().toString(),
				schedule.getEndTime().toString(),
				schedule.getLocation(),
				schedule.getCategory().getName(),
				schedule.getMemo(),
				schedule.getNotificationTime() != null ? schedule.getNotificationTime().name() : null,
				schedule.getRecurrence() != null
					? new Recurrence(frequency, schedule.getRecurrence().getRecurrenceEndDate())
					: null
			);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Recurrence {
		private String frequency;
		private LocalDate recurrenceEndDate;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SchedulesInRange {
		List<ScheduleInRange> schedules;

		public static SchedulesInRange from(List<Schedule> schedules) {
			List<ScheduleInRange> scheduleInRanges = schedules.stream()
				.map(ScheduleInRange::from)
				.collect(Collectors.toList());

			return new SchedulesInRange(scheduleInRanges);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class ScheduleInRange {
		private Long id;
		private String title;
		private String startDate;
		private String endDate;
		private String startTime;
		private String endTime;

		public static ScheduleInRange from(Schedule schedule) {
			return new ScheduleInRange(
				schedule.getId(),
				schedule.getTitle(),
				schedule.getStartDate().toString(),
				schedule.getEndDate().toString(),
				schedule.getStartTime().toString(),
				schedule.getEndTime().toString()
			);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SearchList {
		private List<SearchContent> contents;

		public static SearchList from(List<Schedule> schedules) {
			List<SearchContent> searchContents = schedules.stream()
				.map(SearchContent::from)
				.collect(Collectors.toList());

			return new SearchList(searchContents);
		}
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SearchContent {
		private Long scheduleId;
		private Long calendarId;
		private String calendarName;
		private String title;
		private LocalDateTime startDateTime;
		private LocalDateTime endDateTime;

		public static SearchContent from(Schedule schedule) {
			return new SearchContent(
				schedule.getId(),
				schedule.getCalendar().getId(),
				schedule.getCalendar().getName(),
				schedule.getTitle(),
				schedule.getStartDate().atTime(schedule.getStartTime()),
				schedule.getEndDate().atTime(schedule.getEndTime())
			);
		}
	}
}
