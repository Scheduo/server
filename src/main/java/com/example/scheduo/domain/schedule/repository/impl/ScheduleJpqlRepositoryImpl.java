package com.example.scheduo.domain.schedule.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
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
	public List<Schedule> findSchedulesByStartMonthAndEndMonth(int month){
		String jpql = """
			SELECT s FROM Schedule s
			WHERE MONTH(s.startDate) = :month OR MONTH(s.endDate) = :date
			AND s.recurrence is null
			""";
		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("month", month)
			.getResultList();
	}

	@Override
	public List<Schedule> findSchedulesWithRecurrence(int month) {
		String jpql = """
   			SELECT s FROM Schedule s
   			JOIN FETCH s.recurrence r
   			WHERE MONTH(r.recurrenceEndDate) >= :month
   			AND MONTH(s.startDate) <= :month
			""";

		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("month", month)
			.getResultList();
	}

}
