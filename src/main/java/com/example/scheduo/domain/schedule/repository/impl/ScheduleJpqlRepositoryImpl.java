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
	public List<Schedule> findSchedulesByStartMonthAndEndMonth(String month){
		String jpql = """
			SELECT s FROM Schedule s
			WHERE MONTH(s.startDate) = :month OR MONTH(s.endDate) = :date
			""";
		return entityManager.createQuery(jpql, Schedule.class)
			.setParameter("month", month)
			.getResultList();
	}
}
