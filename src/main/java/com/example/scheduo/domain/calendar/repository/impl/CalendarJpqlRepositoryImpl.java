package com.example.scheduo.domain.calendar.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.repository.CalendarJpqlRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class CalendarJpqlRepositoryImpl implements CalendarJpqlRepository {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Optional<Calendar> findByIdWithParticipants(Long calendarId) {
		String jpql = """
				SELECT c FROM Calendar c
				LEFT JOIN FETCH c.participants
				WHERE c.id = :calendarId
			""";
		return entityManager.createQuery(jpql, Calendar.class)
			.setParameter("calendarId", calendarId)
			.getResultStream()
			.findFirst();
	}
}
