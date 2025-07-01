package com.example.scheduo.domain.schedule.service.Impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto;
import com.example.scheduo.domain.schedule.entity.Category;
import com.example.scheduo.domain.schedule.entity.Recurrence;
import com.example.scheduo.domain.schedule.entity.Schedule;
import com.example.scheduo.domain.schedule.repository.CategoryRepository;
import com.example.scheduo.domain.schedule.repository.ScheduleRepository;
import com.example.scheduo.domain.schedule.service.ScheduleService;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleServiceImpl implements ScheduleService {
	private final ScheduleRepository scheduleRepository;
	private final CategoryRepository categoryRepository;
	private final CalendarRepository calendarRepository;

	@Override
	@Transactional
	public void createSchedule(ScheduleRequestDto.Create request, Member member, Long calendarId) {
		Calendar calendar = calendarRepository.findById(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		Category category = categoryRepository.findByName(request.getCategory())
			.orElseThrow(() -> new ApiException(ResponseStatus.CATEGORY_NOT_FOUND));

		Recurrence recurrence = null;
		if (request.getRecurrence() != null) {
			recurrence = Recurrence.create(
				request.getRecurrence().getFrequency(),
				request.getRecurrence().getRecurrenceEndDate()
			);
		}

		Schedule schedule = Schedule.create(
			request.getTitle(),
			request.isAllDay(),
			request.getStartDate(),
			request.getEndDate(),
			request.getStartTime(),
			request.getEndTime(),
			request.getLocation(),
			request.getMemo(),
			request.getNotificationTime(),
			category,
			member,
			calendar,
			recurrence
		);

		scheduleRepository.save(schedule);
	}
}
