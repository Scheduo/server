package com.example.scheduo.domain.calendar.service;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.dto.CalendarResponseDto;
import com.example.scheduo.domain.member.entity.Member;

public interface CalendarService {
	void inviteMember(Long calendarId, Member inviter, Long inviteeId);

	void acceptInvitation(Long calendarId, Member member);

	void rejectInvitation(Long calendarId, Member member);

	CalendarResponseDto.CalendarInfo createCalendar(CalendarRequestDto.Create calendarInfo, Member member);

	void editCalendar(CalendarRequestDto.Edit editInfo, Long calendarId, Member member);

	void deleteCalendar(Long calendarId, Member member);

	CalendarResponseDto.CalendarInfoList getCalendars(Member member);

	void updateParticipantRole(Long calendarId, Long participantId, CalendarRequestDto.UpdateParticipantRole request, Long requesterId);

	void removeParticipant(Long calendarId, Long participantId, Long requesterId);

	CalendarResponseDto.CalendarDetailInfo getCalendar(Long calendarId, Member member);
}
