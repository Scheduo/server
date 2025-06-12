package com.example.scheduo.domain.calendar.repository;

import java.util.Optional;

import com.example.scheduo.domain.calendar.entity.Calendar;

public interface CalendarJpqlRepository {
	Optional<Calendar> findByIdWithParticipants(Long calendarId);

	Optional<Calendar> findByIdWithParticipantsAndMembers(Long calendarId);
}
