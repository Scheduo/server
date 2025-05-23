package com.example.scheduo.domain.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CalendarRequestDto {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Invite {
		private Long memberId;
	}
}
