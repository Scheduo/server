package com.example.scheduo.domain.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scheduo.domain.calendar.entity.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
