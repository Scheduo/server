package com.example.scheduo.domain.calendar.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.dto.CalendarResponseDto;
import com.example.scheduo.domain.calendar.service.CalendarService;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.global.auth.annotation.RequestMember;
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
	@Operation(summary = "캘린더 생성", description = "사용자의 캘린더를 생성합니다.")
	public ApiResponse<CalendarResponseDto.CalendarInfo> create(
		@RequestMember Member member,
		@Valid @RequestBody CalendarRequestDto.Create request
	) {
		CalendarResponseDto.CalendarInfo calendarInfo = calendarService.createCalendar(request, member);
		return ApiResponse.onSuccess(calendarInfo);
	}

	@PostMapping("/{calendarId}/invite")
	@Operation(summary = "캘린더 초대", description = "캘린더에 사용자를 초대합니다.")
	public ApiResponse<?> invite(@PathVariable("calendarId") Long calendarId,
		@RequestBody CalendarRequestDto.Invite request,
		@RequestMember Member member) {
		calendarService.inviteMember(calendarId, member, request.getMemberId());
		return ApiResponse.onSuccess();
	}

	@PostMapping("/{calendarId}/invite/accept")
	@Operation(summary = "캘린더 초대 수락", description = "캘린더 초대를 수락합니다.")
	public ApiResponse<?> accept(@PathVariable("calendarId") Long calendarId,
		@RequestMember Member member) {
		calendarService.acceptInvitation(calendarId, member);
		return ApiResponse.onSuccess();
	}

	@PostMapping("/{calendarId}/invite/decline")
	@Operation(summary = "캘린더 초대 거절", description = "캘린더 초대를 거절합니다.")
	public ApiResponse<?> reject(@PathVariable("calendarId") Long calendarId,
		@RequestMember Member member) {
		calendarService.rejectInvitation(calendarId, member);
		return ApiResponse.onSuccess();
	}

	@PatchMapping("/{calendarId}")
	@Operation(summary = "캘린더 편집", description = "사용자의 캘린더 정보를 수정합니다. (캘린더 제목은 오너만 가능) (닉네임은 참여자만 가능)")
	public ApiResponse<?> edit(@PathVariable("calendarId") Long calendarId,
		@RequestBody CalendarRequestDto.Edit editInfo,
		@RequestMember Member member) {
		calendarService.editCalendar(editInfo, calendarId, member);
		return ApiResponse.onSuccess();
	}

	@DeleteMapping("/{calendarId}")
	@Operation(summary = "캘린더 삭제", description = "사용자의 캘린더를 삭제합니다. (오너만 가능)")
	public ApiResponse<?> delete(@PathVariable("calendarId") Long calendarId,
		@RequestMember Member member) {
		calendarService.deleteCalendar(calendarId, member);
		return ApiResponse.onSuccess();
	}

	@GetMapping()
	@Operation(summary = "모든 캘린더 조회", description = "사용자의 모든 캘린더를 조회합니다.")
	public ApiResponse<CalendarResponseDto.CalendarInfoList> get(@RequestMember Member member) {
		CalendarResponseDto.CalendarInfoList calendars = calendarService.getCalendars(member);
		return ApiResponse.onSuccess(calendars);
	}

	@PatchMapping("/{calendarId}/participants/{participantId}")
	@Operation(summary = "참여자 권한 수정", description = "캘린더 참여자의 권한을 수정합니다. (오너만 가능)")
	public ApiResponse<?> updateParticipantRole(
		@PathVariable("calendarId") Long calendarId,
		@PathVariable("participantId") Long participantId,
		@Valid @RequestBody CalendarRequestDto.UpdateParticipantRole request,
		@RequestMember Member member
	) {
		calendarService.updateParticipantRole(calendarId, participantId, request, member.getId());
		return ApiResponse.onSuccess();
	}

	@DeleteMapping("/{calendarId}/participants/{participantId}")
	@Operation(summary = "참여자 내보내기", description = "캘린더에서 참여자를 내보냅니다. (오너만 가능)")
	public ApiResponse<?> removeParticipant(
		@PathVariable("calendarId") Long calendarId,
		@PathVariable("participantId") Long participantId,
		@RequestMember Member member
	) {
		calendarService.removeParticipant(calendarId, participantId, member.getId());
		return ApiResponse.onSuccess();
	}

	@GetMapping("/{calendarId}")
	public ApiResponse<CalendarResponseDto.CalendarDetailInfo> get(@PathVariable("calendarId") Long calendarId,
		@RequestMember Member member) {
		CalendarResponseDto.CalendarDetailInfo calendar = calendarService.getCalendar(calendarId, member);
		return ApiResponse.onSuccess(calendar);
	}
}
