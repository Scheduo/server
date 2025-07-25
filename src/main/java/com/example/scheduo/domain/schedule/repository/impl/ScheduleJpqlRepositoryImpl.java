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
	public List<Schedule> findSchedulesByDateRange(LocalDate startOfMonth, LocalDate endOfMonth, long calendarId) {
		String jpql = """
			SELECT s FROM Schedule s
			JOIN FETCH s.category c
			WHERE ((s.startDate >= :startOfMonth AND s.startDate <= :endOfMonth)
				   OR (s.endDate >= :startOfMonth AND s.endDate <= :endOfMonth)
				   OR (s.startDate <= :startOfMonth AND s.endDate >= :endOfMonth))
			AND s.recurrence is null
			AND s.calendar.id = :calendarId
        	""";
		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("startOfMonth", startOfMonth)
			.setParameter("endOfMonth", endOfMonth)
			.setParameter("calendarId", calendarId)
			.getResultList();
	}

	@Override
	public List<Schedule> findSchedulesWithRecurrenceForRange(LocalDate firstDayOfMonth, LocalDate lastDayOfMonth,
		long calendarId) {
		String jpql = """
				SELECT DISTINCT s FROM Schedule s
				JOIN FETCH s.recurrence r
				JOIN FETCH s.category c
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

	@Override
	public List<Schedule> findSchedulesByDate(LocalDate date, long calendarId) {
		String jpql = """
            SELECT s FROM Schedule s
            JOIN FETCH s.category c
            WHERE s.startDate <= :date 
            AND s.endDate >= :date
            AND s.recurrence is null
            AND s.calendar.id = :calendarId
            """;
		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("date", date)
			.setParameter("calendarId", calendarId)
			.getResultList();
	}

	@Override
	public List<Schedule> findSchedulesWithRecurrenceForDate(LocalDate date, long calendarId) {
		String jpql = """
            SELECT DISTINCT s FROM Schedule s
            JOIN FETCH s.recurrence r
            JOIN FETCH s.category c
            WHERE s.startDate <= :date
            AND (r.recurrenceEndDate IS NULL OR r.recurrenceEndDate >= :date)
            AND s.calendar.id = :calendarId
            """;
		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("date", date)
			.setParameter("calendarId", calendarId)
			.getResultList();
	}

}
