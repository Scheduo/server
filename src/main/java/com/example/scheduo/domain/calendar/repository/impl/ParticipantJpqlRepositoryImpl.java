package com.example.scheduo.domain.calendar.repository.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.entity.Role;
import com.example.scheduo.domain.calendar.repository.ParticipantJpqlRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Repository
public class ParticipantJpqlRepositoryImpl implements ParticipantJpqlRepository {
	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public Optional<Participant> findOwnerByCalendarId(Long calendarId) {
		String jpql = """
				SELECT p FROM Participant p
				LEFT JOIN FETCH p.member m
				WHERE p.calendar.id = :calendarId AND p.role = :role
			""";
		return entityManager.createQuery(jpql, Participant.class)
			.setParameter("calendarId", calendarId)
			.setParameter("role", Role.OWNER)
			.getResultStream()
			.findFirst();
	}
}
