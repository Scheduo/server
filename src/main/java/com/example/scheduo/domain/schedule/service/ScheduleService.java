package com.example.scheduo.domain.schedule.service;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto;

public interface ScheduleService {
	void createSchedule(ScheduleRequestDto.Create request, Member member, Long calendarId);
}
