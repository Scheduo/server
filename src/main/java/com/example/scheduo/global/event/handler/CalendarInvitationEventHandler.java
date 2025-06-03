package com.example.scheduo.global.event.handler;

import java.util.Map;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.scheduo.domain.member.entity.NotificationType;
import com.example.scheduo.domain.member.service.NotificationService;
import com.example.scheduo.global.event.CalendarInvitationEvent;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CalendarInvitationEventHandler {
	private final NotificationService notificationService;

	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(CalendarInvitationEvent event) throws InterruptedException {
		notificationService.createNotification(
			event.getInvitee(),
			NotificationType.CALENDAR_INVITATION,
			Map.of(
				"calendarId", event.getCalendarId(),
				"calendarName", event.getCalendarName(),
				"inviterName", event.getInviterName()
			)

		);
	}

}
