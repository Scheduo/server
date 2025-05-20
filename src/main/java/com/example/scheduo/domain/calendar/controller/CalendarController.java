package com.example.scheduo.domain.calendar.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.dto.CalendarResponseDto;
import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.service.CalendarService;
import com.example.scheduo.global.response.ApiResponse;
import com.example.scheduo.global.response.status.ResponseStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CalendarController {

	private final CalendarService calendarService;

	@PostMapping
	public ApiResponse<CalendarResponseDto.CalendarInfo> createCalendar(
		@Valid @RequestBody CalendarRequestDto.Create request
	) {
		//로그인 반영 후 memberId 수정 예정
		Long memberId = 1L;

		Calendar calendar = calendarService.createCalendar(request, memberId);

		return ApiResponse.onSuccess(ResponseStatus.CREATED_CALENDAR,
			CalendarResponseDto.CalendarInfo.fromEntity(calendar));
	}
}
