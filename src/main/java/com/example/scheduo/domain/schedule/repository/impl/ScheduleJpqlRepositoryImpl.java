package com.example.scheduo.domain.schedule.repository.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
			WHERE (s.startDate <= :endOfMonth AND s.endDate >= :startOfMonth)
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

	@Override
	public Optional<Schedule> findScheduleByIdFetchJoin(Long scheduleId) {
		String jpql = """
			SELECT s FROM Schedule s
			JOIN FETCH s.category c
			JOIN FETCH s.calendar cal
			LEFT JOIN FETCH s.recurrence r
			WHERE s.id = :scheduleId
			""";
		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("scheduleId", scheduleId)
			.getResultStream()
			.findFirst();
	}

	@Override
	public List<Schedule> searchByMemberIdAndKeywordPrefix(Long memberId, String keyword) {
		// prefix 검색을 위해 keyword% 형태로 바인딩
		String likeParam = keyword + "%";
		String jpql = """
			SELECT s FROM Schedule s
			JOIN FETCH s.calendar c
			JOIN c.participants p
			WHERE p.member.id = :memberId
				AND lower(s.title) LIKE lower(:kw)
			ORDER BY s.startDate, s.startTime, s.id
			""";

		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("memberId", memberId)
			.setParameter("kw", likeParam)
			.getResultList();
	}
}
