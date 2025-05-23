package com.example.scheduo.domain.calendar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.scheduo.domain.calendar.entity.Calendar;

public interface CalendarRepository extends JpaRepository<Calendar, Long> {
	@Query("SELECT c FROM Calendar c LEFT JOIN FETCH c.participants WHERE c.id = :calendarId")
	Optional<Calendar> findByIdWithParticipants(Long calendarId);
}
