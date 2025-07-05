package com.example.scheduo.domain.calendar.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.example.scheduo.domain.calendar.entity.Calendar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CalendarResponseDto {

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CalendarInfo {
		private Long calendarId;
		private String title;

		public static CalendarInfo from(Calendar calendar) {
			return CalendarInfo.builder()
				.calendarId(calendar.getId())
				.title(calendar.getName())
				.build();
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CalendarInfoList {
		private List<CalendarInfo> calendars;

		public static CalendarInfoList from(List<Calendar> calendars) {
			List<CalendarInfo> calendarInfoList = calendars.stream()
				.map(CalendarInfo::from)
				.collect(Collectors.toList());
			return CalendarInfoList.builder()
				.calendars(calendarInfoList)
				.build();
		}
	}
}
