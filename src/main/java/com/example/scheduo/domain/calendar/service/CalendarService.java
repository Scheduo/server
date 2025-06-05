package com.example.scheduo.domain.calendar.service;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.dto.CalendarResponseDto;
import com.example.scheduo.domain.member.entity.Member;

public interface CalendarService {
	void inviteMember(Long calendarId, Member inviter, Long inviteeId);

	void acceptInvitation(Long calendarId, Member member);

	void rejectInvitation(Long calendarId, Member member);

	CalendarResponseDto.CalendarInfo createCalendar(CalendarRequestDto.Create calendarInfo, Long memberId);

	void editCalendar(CalendarRequestDto.Edit editInfo, Long calendarId, Long memberId);

	void deleteCalendar(Long calendarId, Long memberId);
}
