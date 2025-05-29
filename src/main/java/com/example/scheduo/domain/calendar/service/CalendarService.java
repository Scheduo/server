package com.example.scheduo.domain.calendar.service;

public interface CalendarService {
	void inviteMember(Long calendarId, Long inviterId, Long inviteeId);

	void acceptInvitation(Long calendarId, Long memberId);

	void rejectInvitation(Long calendarId, Long memberId);
}
