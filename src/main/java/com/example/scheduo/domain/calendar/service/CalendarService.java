package com.example.scheduo.domain.calendar.service;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.dto.CalendarResponseDto;

public interface CalendarService {
	void inviteMember(Long calendarId, Long inviterId, Long inviteeId);

	void acceptInvitation(Long calendarId, Long memberId);

	void rejectInvitation(Long calendarId, Long memberId);

	CalendarResponseDto.CalendarInfo createCalendar(CalendarRequestDto.Create calendarInfo, Long memberId);
}
