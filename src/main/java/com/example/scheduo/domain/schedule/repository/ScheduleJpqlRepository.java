package com.example.scheduo.domain.schedule.repository;

import java.time.LocalDate;
import java.util.List;

import com.example.scheduo.domain.schedule.entity.Schedule;

public interface ScheduleJpqlRepository {
	// 월별 일정(반복 x) 조회
	List<Schedule> findSchedulesByDateRange(LocalDate startOfMonth, LocalDate endOfMonth, long calendarId);

	// 반복 일정 중 조회 유효한 일정 조회
	List<Schedule> findSchedulesWithRecurrenceForRange(LocalDate firstDayOfMonth, LocalDate lastDayOfMonth, long calendarId);

	//특정 날짜의 단일 일정 조회(반복 x)
	List<Schedule> findSchedulesByDate(LocalDate date, long calendarId);

	// 특정 날짜에 해당할 수 있는 반복 일정 조회
	List<Schedule> findSchedulesWithRecurrenceForDate(LocalDate date, long calendarId);
}
