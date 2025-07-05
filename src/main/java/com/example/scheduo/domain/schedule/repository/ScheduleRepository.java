package com.example.scheduo.domain.schedule.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scheduo.domain.schedule.entity.Schedule;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
}
