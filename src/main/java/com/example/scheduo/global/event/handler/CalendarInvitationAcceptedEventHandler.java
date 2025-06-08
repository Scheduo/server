package com.example.scheduo.global.event.handler;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.calendar.repository.ParticipantRepository;
import com.example.scheduo.domain.member.entity.NotificationType;
import com.example.scheduo.domain.member.service.NotificationService;
import com.example.scheduo.global.event.CalendarInvitationAcceptedEvent;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CalendarInvitationAcceptedEventHandler {
	private final CalendarRepository calendarRepository;
	private final ParticipantRepository participantRepository;
	private final NotificationService notificationService;

	@Async
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(CalendarInvitationAcceptedEvent event) {
		long calendarId = event.getCalendarId();
		Calendar calendar = calendarRepository.findById(calendarId)
			.orElseThrow(() -> new IllegalArgumentException(ResponseStatus.CALENDAR_NOT_FOUND.getMessage()));

		Participant participant = participantRepository.findOwnerByCalendarId(calendarId).orElseThrow(() ->
			new IllegalArgumentException(ResponseStatus.MEMBER_NOT_OWNER.getMessage()));

		notificationService.createNotification(
			participant.getMember(),
			NotificationType.CALENDAR_INVITATION_ACCEPTED,
			Map.of(
				"inviteeNickname", event.getInvitee().getNickname(),
				"calendarName", calendar.getName()
			)
		);
	}

}
