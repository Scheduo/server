package com.example.scheduo.domain.schedule.service;

import java.util.List;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto;
import com.example.scheduo.domain.schedule.dto.ScheduleResponseDto;

public interface ScheduleService {
	void createSchedule(ScheduleRequestDto.Create request, Member member, Long calendarId);

	ScheduleResponseDto.SchedulesOnMonth getSchedulesOnMonth(Member member, Long calendarId, String date);

	ScheduleResponseDto.SchedulesOnDate getSchedulesOnDate(Member member, Long calendarId, String date);

	ScheduleResponseDto.ScheduleInfo getScheduleInfo(Member member, Long calendarId, Long scheduleId, String date);

	void updateSchedule(ScheduleRequestDto.Update request, Member member, Long calendarId, Long scheduleId,
		String date);

	ScheduleResponseDto.SchedulesInRange getSchedulesInRange(Member member, Long calendarId, String startDate, String endDate);

	void shareSchedule(Member member, Long calendarId, Long targetCalendarId, List<ScheduleRequestDto.ScheduleTime> schedules);

	ScheduleResponseDto.SearchList searchSchedules(Member member, String keyword);
}
