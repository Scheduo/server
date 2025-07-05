package com.example.scheduo.domain.schedule.repository;

import java.util.List;

import com.example.scheduo.domain.schedule.entity.Schedule;

public interface ScheduleJpqlRepository {
	// 월별 일정 조회
	List<Schedule> findSchedulesByStartMonthAndEndMonth(String date);
}
