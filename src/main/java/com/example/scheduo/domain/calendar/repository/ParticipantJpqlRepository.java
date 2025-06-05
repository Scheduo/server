package com.example.scheduo.domain.calendar.repository;

import java.util.List;
import java.util.Optional;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;

public interface ParticipantJpqlRepository {
	Optional<Participant> findOwnerByCalendarId(Long calendarId);

	List<Calendar> findCalendarsByMemberId(Long memberId);
}
