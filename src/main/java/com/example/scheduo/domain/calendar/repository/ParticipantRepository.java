package com.example.scheduo.domain.calendar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scheduo.domain.calendar.entity.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {

	Optional<Participant> findByCalendarIdAndMemberId(Long calendarId, Long memberId);
}
