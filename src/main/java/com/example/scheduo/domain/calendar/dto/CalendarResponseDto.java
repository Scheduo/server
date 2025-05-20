package com.example.scheduo.domain.calendar.dto;

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
		private String calendarTitle;

		public static CalendarInfo fromEntity(Calendar calendar) {
			return new CalendarInfo(calendar.getId(), calendar.getName());
		}
	}
}
