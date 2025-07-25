package com.example.scheduo.domain.schedule.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto;
import com.example.scheduo.domain.schedule.dto.ScheduleResponseDto;
import com.example.scheduo.domain.schedule.service.ScheduleService;
import com.example.scheduo.global.auth.annotation.RequestMember;
import com.example.scheduo.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "일정 관련 API")
public class ScheduleController {
	private final ScheduleService scheduleService;

	@PostMapping("/calendars/{calendarId}/schedules")
	@Operation(summary = "일정 생성", description = "해당 캘린더에 일정을 생성합니다.")
	public ApiResponse<?> createSchedule(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@Valid @RequestBody ScheduleRequestDto.Create request
	) {
		scheduleService.createSchedule(request, member, calendarId);
		return ApiResponse.onSuccess();
	}

	@GetMapping("/calendars/{calendarId}/schedules/monthly")
	public ApiResponse<ScheduleResponseDto.SchedulesOnMonth> getScheduleByMonthly(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@RequestParam("date") String date
	) {
		ScheduleResponseDto.SchedulesOnMonth res = scheduleService.getSchedulesOnMonth(member, calendarId, date);
		return ApiResponse.onSuccess(res);
	}

	@GetMapping("/calendars/{calendarId}/schedules")
	public ApiResponse<ScheduleResponseDto.SchedulesOnDate> getScheduleOnDateInCalendar(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@RequestParam("date") String date
	) {
		return ApiResponse.onSuccess();
	}

}