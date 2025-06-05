package com.example.scheduo.global.event;

import com.example.scheduo.domain.member.entity.Member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CalendarInvitationAcceptedEvent {
	private long calendarId;
	//TODO: 나중에 Member로 변경 or InviteeNickname 으로 변경
	private Member invitee;
}
