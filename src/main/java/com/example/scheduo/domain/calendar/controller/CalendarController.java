package com.example.scheduo.domain.calendar.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.dto.CalendarResponseDto;
import com.example.scheduo.domain.calendar.service.CalendarService;
import com.example.scheduo.global.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/calendars")
@Tag(name = "Calendar", description = "캘린더 관련 API")
public class CalendarController {

	private final CalendarService calendarService;

	@PostMapping()
	public ApiResponse<CalendarResponseDto.CalendarInfo> create(
		Authentication authentication,
		@Valid @RequestBody CalendarRequestDto.Create request
	) {
		Long memberId = (Long)authentication.getPrincipal();
		CalendarResponseDto.CalendarInfo calendarInfo = calendarService.createCalendar(request, memberId);
		return ApiResponse.onSuccess(calendarInfo);
	}

	@PostMapping("/{calendarId}/invite")
	@Operation(summary = "캘린더 초대", description = "캘린더에 사용자를 초대합니다.")
	public ApiResponse<?> invite(@PathVariable("calendarId") Long calendarId,
		@RequestBody CalendarRequestDto.Invite request,
		Authentication authentication) {
		Long memberId = (Long)authentication.getPrincipal();
		calendarService.inviteMember(calendarId, memberId, request.getMemberId());
		return ApiResponse.onSuccess();
	}

	@PostMapping("/{calendarId}/invite/accept")
	@Operation(summary = "캘린더 초대 수락", description = "캘린더 초대를 수락합니다.")
	public ApiResponse<?> accept(@PathVariable("calendarId") Long calendarId,
		Authentication authentication) {
		Long memberId = (Long)authentication.getPrincipal();
		calendarService.acceptInvitation(calendarId, memberId);
		return ApiResponse.onSuccess();
	}

	@PostMapping("/{calendarId}/invite/decline")
	@Operation(summary = "캘린더 초대 거절", description = "캘린더 초대를 거절합니다.")
	public ApiResponse<?> reject(@PathVariable("calendarId") Long calendarId,
		@RequestParam("memberId") Long memberId) {
		//Todo: 추후에 AuthenticationContext에서 memberId를 가져와서 사용하도록 수정
		calendarService.rejectInvitation(calendarId, memberId);
		return ApiResponse.onSuccess();
	}

	@PatchMapping("/{calendarId}")
	public ApiResponse<?> edit(@PathVariable("calendarId") Long calendarId,
		@RequestBody CalendarRequestDto.Edit editInfo,
		Authentication authentication) {
		Long memberId = (Long)authentication.getPrincipal();
		calendarService.editCalendar(editInfo, calendarId, memberId);
		return ApiResponse.onSuccess();
	}
}
