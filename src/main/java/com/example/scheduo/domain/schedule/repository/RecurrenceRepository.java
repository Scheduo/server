package com.example.scheduo.domain.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scheduo.domain.schedule.entity.Recurrence;

public interface RecurrenceRepository extends JpaRepository<Recurrence, Long> {
}
