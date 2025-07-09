package com.example.scheduo.domain.schedule.repository.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.scheduo.domain.schedule.entity.Schedule;
import com.example.scheduo.domain.schedule.repository.ScheduleJpqlRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class ScheduleJpqlRepositoryImpl implements ScheduleJpqlRepository {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Schedule> findSchedulesByStartMonthAndEndMonth(int month, long calendarId) {
		String jpql = """
			SELECT s FROM Schedule s
			WHERE (MONTH(s.startDate) = :month OR MONTH(s.endDate) = :month)
			AND s.recurrence is null
			AND s.calendar.id = :calendarId
			""";
		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("month", month)
			.setParameter("calendarId", calendarId)
			.getResultList();
	}

	@Override
	public List<Schedule> findSchedulesWithRecurrence(LocalDate firstDayOfMonth, LocalDate lastDayOfMonth,
		long calendarId) {
		String jpql = """
				SELECT DISTINCT s FROM Schedule s
				JOIN FETCH s.recurrence r
				WHERE r.recurrenceEndDate >= :startOfMonth
				AND s.startDate <= :endOfMonth
				AND s.calendar.id = :calendarId
			""";

		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("startOfMonth", firstDayOfMonth)
			.setParameter("endOfMonth", lastDayOfMonth)
			.setParameter("calendarId", calendarId)
			.getResultList();
	}

}
