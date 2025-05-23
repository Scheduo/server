package com.example.scheduo.domain.calendar.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.service.CalendarService;
import com.example.scheduo.global.response.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendars")
public class CalendarController {
	private final CalendarService calendarService;

	@PostMapping("/{calendarId}/invite")
	public ApiResponse<?> invite(@PathVariable("calendarId") Long calendarId,
		@RequestBody CalendarRequestDto.Invite request,
		@RequestParam("memberId") Long memberId) {
		//Todo: 추후에 AuthenticationContext에서 memberId를 가져와서 사용하도록 수정
		calendarService.inviteMember(calendarId, memberId, request.getMemberId());
		return ApiResponse.onSuccess();
	}
}
