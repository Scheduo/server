package com.example.scheduo.global.event;

import com.example.scheduo.domain.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CalendarInvitationEvent {
	private Member invitee;
	private Long calendarId;
	private String inviterName;
	private String calendarName;
}
