package com.example.scheduo.domain.schedule.service;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto;
import com.example.scheduo.domain.schedule.dto.ScheduleResponseDto;

public interface ScheduleService {
	void createSchedule(ScheduleRequestDto.Create request, Member member, Long calendarId);

	ScheduleResponseDto.SchedulesOnMonth getSchedulesOnMonth(Member member, Long calendarId, String date);

	ScheduleResponseDto.SchedulesOnDate getSchedulesOnDate(Member member, Long calendarId, String date);

	ScheduleResponseDto.ScheduleInfo getScheduleInfo(Member member, Long calendarId, Long scheduleId, String date);
	ScheduleResponseDto.ScheduleInfo getScheduleInfo(Member member, Long calendarId, Long scheduleId);

	void updateSchedule(ScheduleRequestDto.Update request, Member member, Long calendarId, Long scheduleId,
		String date);
}
