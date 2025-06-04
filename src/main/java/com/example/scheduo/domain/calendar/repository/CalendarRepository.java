package com.example.scheduo.domain.calendar.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scheduo.domain.calendar.entity.Calendar;

public interface CalendarRepository extends JpaRepository<Calendar, Long>, CalendarJpqlRepository {
}
