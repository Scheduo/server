package com.example.scheduo.domain.schedule.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

	@Operation(summary = "일정 월별 조회", description = "해당 캘린더의 월별 일정을 조회합니다.")
	@GetMapping("/calendars/{calendarId}/schedules/monthly")
	public ApiResponse<ScheduleResponseDto.SchedulesOnMonth> getScheduleOnMonth(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@RequestParam("date") String date
	) {
		ScheduleResponseDto.SchedulesOnMonth res = scheduleService.getSchedulesOnMonth(member, calendarId, date);
		return ApiResponse.onSuccess(res);
	}

	@Operation(summary = "캘린더 별 특정 날짜의 일정 조회", description = "해당 캘린더의 특정 날짜의 모든 일정을 조회합니다.")
	@GetMapping("/calendars/{calendarId}/schedules")
	public ApiResponse<ScheduleResponseDto.SchedulesOnDate> getScheduleOnDate(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@RequestParam("date") String date
	) {
		ScheduleResponseDto.SchedulesOnDate res = scheduleService.getSchedulesOnDate(member, calendarId, date);
		return ApiResponse.onSuccess(res);
	}

	@Operation(summary = "일정 상세 조회", description = "해당 일정의 상세 정보를 조회합니다.")
	@GetMapping("/calendars/{calendarId}/schedules/{scheduleId}")
	public ApiResponse<ScheduleResponseDto.ScheduleInfo> getScheduleInfo(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@PathVariable("scheduleId") Long scheduleId,
		@RequestParam("date") String date
	) {
		ScheduleResponseDto.ScheduleInfo result = scheduleService.getScheduleInfo(member, calendarId, scheduleId, date);
		return ApiResponse.onSuccess(result);
	}

	@PatchMapping("/calendars/{calendarId}/schedules/{scheduleId}")
	@Operation(summary = "일정 수정", description = "해당 일정의 정보를 수정합니다.")
	public ApiResponse<?> updateSchedule(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@PathVariable("scheduleId") Long scheduleId,
		@RequestParam("date") String date,
		@Valid @RequestBody ScheduleRequestDto.Update request
	) {
		scheduleService.updateSchedule(request, member, calendarId, scheduleId, date);
		return ApiResponse.onSuccess();
	}

	@Operation(summary = "캘린더 별 기간별 일정 조회", description = "해당 캘린더의 지정된 기간 동안의 모든 일정을 조회합니다.")
	@GetMapping("/calendars/{calendarId}/schedules/range")
	public ApiResponse<ScheduleResponseDto.SchedulesInRange> getSchedulesInRange(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@RequestParam("startDate") String startDate,
		@RequestParam("endDate") String endDate
	) {
		ScheduleResponseDto.SchedulesInRange res = scheduleService.getSchedulesInRange(member, calendarId, startDate,
			endDate);
		return ApiResponse.onSuccess(res);
	}

	@Operation(summary = "일정 공유", description = "캘린더 간 일정 공유를 합니다.")
	@PostMapping("/calendars/{calendarId}/schedules/share")
	public ApiResponse<?> shareSchedule(
		@RequestMember Member member,
		@PathVariable("calendarId") Long calendarId,
		@RequestBody ScheduleRequestDto.Share req
	) {
		scheduleService.shareSchedule(member, calendarId, req.getTargetCalendarId(), req.getSchedules());
		return ApiResponse.onSuccess();
	}
}