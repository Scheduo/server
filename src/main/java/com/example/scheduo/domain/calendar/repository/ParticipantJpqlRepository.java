package com.example.scheduo.domain.calendar.repository;

import java.util.Optional;

import com.example.scheduo.domain.calendar.entity.Participant;

public interface ParticipantJpqlRepository {
	Optional<Participant> findOwnerByCalendarId(Long calendarId);
}
