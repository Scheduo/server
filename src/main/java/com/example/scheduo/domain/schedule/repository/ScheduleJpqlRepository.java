package com.example.scheduo.domain.schedule.repository;

import java.time.LocalDate;
import java.util.List;

import com.example.scheduo.domain.schedule.entity.Schedule;

public interface ScheduleJpqlRepository {
	// 월별 일정(반복 x) 조회
	List<Schedule> findSchedulesByStartMonthAndEndMonth(int month, long calendarId);

	// 반복 일정 중 조회 유효한 일정 조회
	List<Schedule> findSchedulesWithRecurrence(LocalDate firstDayOfMonth, LocalDate lastDayOfMonth, long calendarId);
}
