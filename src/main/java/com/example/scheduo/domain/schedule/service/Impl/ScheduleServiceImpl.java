package com.example.scheduo.domain.schedule.service.Impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto;
import com.example.scheduo.domain.schedule.dto.ScheduleResponseDto;
import com.example.scheduo.domain.schedule.entity.Category;
import com.example.scheduo.domain.schedule.entity.Recurrence;
import com.example.scheduo.domain.schedule.entity.Schedule;
import com.example.scheduo.domain.schedule.repository.CategoryRepository;
import com.example.scheduo.domain.schedule.repository.RecurrenceRepository;
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
	private final RecurrenceRepository recurrenceRepository;

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
			recurrence = recurrenceRepository.save(recurrence);
		}

		Schedule schedule = Schedule.create(
			request.getTitle(),
			request.isAllDay(),
			request.getStartDateTime(),
			request.getEndDateTime(),
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

	// TODO: 예외 조건 추가(rrule - exception 테이블) (수정 api 선 작업 후 진행)
	@Override
	public ScheduleResponseDto.SchedulesOnMonth getSchedulesOnMonth(Member member, Long calendarId, String date) {
		// 캘린더 조회
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		// 멤버가 캘린더에 속해있는지 검증
		if (!calendar.validateParticipant(member.getId()))
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);

		LocalDate parsedDate = LocalDate.parse(date);
		LocalDate firstDayOfMonth = parsedDate.withDayOfMonth(1);
		LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());

		// 단일 일정 조회
		List<Schedule> schedulesInSingle = scheduleRepository.findSchedulesByDateRange(
			firstDayOfMonth, lastDayOfMonth, calendarId);

		// recurrence에서 enddate를 기준으로 지나지 않은 일정 조회
		List<Schedule> schedulesWithRecurrence = scheduleRepository.findSchedulesWithRecurrenceForRange(firstDayOfMonth,
			lastDayOfMonth, calendarId);

		// recurrence 인스턴스 일정 생성
		List<Schedule> allSchedules = Stream.concat(
			schedulesInSingle.stream(),
			schedulesWithRecurrence.stream().flatMap(s -> s.createSchedulesFromRecurrence().stream())
		).toList();

		int year = parsedDate.getYear();
		int month = parsedDate.getMonthValue();
		List<Schedule> filteredSchedules = new ArrayList<>(allSchedules.stream()
			.filter(s -> (s.getStart().getYear() == year && s.getStart().getMonthValue() == month) ||
				(s.getEnd().getYear() == year && s.getEnd().getMonthValue() == month))
			.toList());

		// filteredSchedules.sort((s1, s2) -> {
		// 	if (s1.getStartDate().equals(s2.getStartDate())) {
		// 		return s1.getStartTime().compareTo(s2.getStartTime());
		// 	}
		// 	return s1.getStartDate().compareTo(s2.getStartDate());
		// });

		return ScheduleResponseDto.SchedulesOnMonth.from(calendarId, filteredSchedules);
	}

	@Override
	public ScheduleResponseDto.ScheduleInfo getScheduleInfo(Member member, Long calendarId, Long scheduleId,
		String date) {
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		if (!calendar.validateParticipant(member.getId())) {
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);
		}

		Schedule schedule = scheduleRepository.findScheduleByIdFetchJoin(scheduleId)
			.orElseThrow(() -> new ApiException(ResponseStatus.SCHEDULE_NOT_FOUND));

		if (!schedule.getCalendar().getId().equals(calendarId)) {
			throw new ApiException(ResponseStatus.SCHEDULE_NOT_FOUND);
		}

		LocalDate parsedDate = LocalDate.parse(date);
		if (!schedule.includesDate(parsedDate)) {
			return ScheduleResponseDto.ScheduleInfo.from(schedule, null);
		}
		return ScheduleResponseDto.ScheduleInfo.from(schedule, date);
	}

	@Override
	@Transactional
	public ScheduleResponseDto.SchedulesOnDate getSchedulesOnDate(Member member, Long calendarId, String date) {
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		if (!calendar.validateParticipant(member.getId()))
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);

		// 특정 날짜 파싱
		LocalDate targetDate = LocalDate.parse(date);

		// 해당 날짜의 단일 일정 조회
		List<Schedule> schedulesInSingle = scheduleRepository.findSchedulesByDate(targetDate, calendarId);

		// 반복 일정 조회 (해당 날짜가 포함될 수 있는 모든 반복 일정)
		List<Schedule> schedulesWithRecurrence = scheduleRepository.findSchedulesWithRecurrenceForDate(targetDate,
			calendarId);

		List<Schedule> allSchedules = Stream.concat(
			schedulesInSingle.stream(),
			schedulesWithRecurrence.stream().flatMap(s -> s.createSchedulesFromRecurrence().stream())
		).toList();

		// 해당 날짜 필터링 (월별과 다른 부분)
		List<Schedule> filteredSchedules = allSchedules.stream()
			.filter(
				s -> !s.getStart().toLocalDate().isAfter(targetDate) && !s.getEnd().toLocalDate().isBefore(targetDate))
			.collect(Collectors.toList());

		// 타임라인 정렬 로직(기간 > 종일 > 시작시간 > 생성시간)
		filteredSchedules.sort((s1, s2) -> {
			// 1. 기간 일정 우선 정렬
			if (!s1.getStart().toLocalDate().equals(s2.getStart().toLocalDate()))
				return s1.getStart().toLocalDate().compareTo(s2.getStart().toLocalDate());

			// 2. 시작 시간 우선 정렬(종일 일정은 시작시간이 00:00:00 이므로 우선순위)
			if (!s1.getStart().toLocalTime().equals(s2.getStart().toLocalTime()))
				return s1.getStart().toLocalTime().compareTo(s2.getStart().toLocalTime());

			// Title 기준으로 정렬
			return s1.getTitle().compareTo(s2.getTitle());
		});

		return ScheduleResponseDto.SchedulesOnDate.from(filteredSchedules);
	}

	@Override
	@Transactional
	public void updateSchedule(ScheduleRequestDto.Update request, Member member, Long calendarId, Long scheduleId,
		String date) {
		Schedule schedule = scheduleRepository.findScheduleByIdFetchJoin(scheduleId)
			.orElseThrow(() -> new ApiException(ResponseStatus.SCHEDULE_NOT_FOUND));

		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		if (!schedule.getCalendar().getId().equals(calendarId)) {
			throw new ApiException(ResponseStatus.SCHEDULE_NOT_FOUND);
		}

		if (!calendar.canEdit(member.getId())) {
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);
		}

		Category category;
		if (!schedule.getCategory().getName().equals(request.getCategory())) {
			category = categoryRepository.findByName(request.getCategory())
				.orElseThrow(() -> new ApiException(ResponseStatus.CATEGORY_NOT_FOUND));
		} else {
			category = schedule.getCategory();
		}
		// 단일 일정인 경우
		if (schedule.getRecurrence() == null) {
			schedule.update(
				request.getTitle(),
				request.isAllDay(),
				request.getStartDateTime(),
				request.getEndDateTime(),
				request.getLocation(),
				request.getMemo(),
				request.getNotificationTime(),
				null,
				null,
				category
			);
		} else {
			// 반복 일정인 경우
			// 모든 일정 수정인 경우
			switch (request.getScope()) {
				case ALL -> {
					schedule.update(
						request.getTitle(),
						request.isAllDay(),
						request.getStartDateTime(),
						request.getEndDateTime(),
						request.getLocation(),
						request.getMemo(),
						request.getNotificationTime(),
						request.getRecurrence().getFrequency(),
						request.getRecurrence().getRecurrenceEndDate(),
						category
					);
				}
				// 이 일정만 수정인 경우, Exception 테이블에 추가 + 새로운 단일 일정 생성
				case ONLY_THIS -> {
					Schedule newSchedule = Schedule.create(
						request.getTitle(),
						request.isAllDay(),
						request.getStartDateTime(),
						request.getEndDateTime(),
						request.getLocation(),
						request.getMemo(),
						request.getNotificationTime(),
						category,
						schedule.getMember(),
						schedule.getCalendar(),
						null
					);
					schedule.getRecurrence().addException(date);
					scheduleRepository.save(newSchedule);
				}

				case THIS_AND_FUTURE -> {
					// startDate 기준으로 일정들을 2개로 나누기
					// 기존꺼의 endDate는 startDate로 변경
					schedule.getRecurrence()
						.changeRecurrenceEndDate(request.getStartDateTime().toLocalDate().toString());

					// startDate 이후의 일정들을 새로운 Recurrence로 생성
					Recurrence recurrence = Recurrence.create(
						request.getRecurrence().getFrequency(),
						request.getRecurrence().getRecurrenceEndDate()
					);
					recurrence = recurrenceRepository.save(recurrence);
					Schedule newSchedule = Schedule.create(
						request.getTitle(),
						request.isAllDay(),
						request.getStartDateTime(),
						request.getEndDateTime(),
						request.getLocation(),
						request.getMemo(),
						request.getNotificationTime(),
						category,
						schedule.getMember(),
						schedule.getCalendar(),
						recurrence
					);
					scheduleRepository.save(newSchedule);
				}
			}
		}
	}

	@Override
	public ScheduleResponseDto.SchedulesInRange getSchedulesInRange(Member member, Long calendarId, String startDate,
		String endDate) {
		// 1. 캘린더 확인
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		// 2. 권한 확인
		if (!calendar.validateParticipant(member.getId()))
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);

		// 기간 파싱
		LocalDate rangeStartDate = LocalDate.parse(startDate);
		LocalDate rangeEndDate = LocalDate.parse(endDate);

		// 3. 기간 동안의 단일 일정 확인
		List<Schedule> schedulesInRange = scheduleRepository.findSchedulesByDateRange(rangeStartDate, rangeEndDate,
			calendarId);

		// 4. 반복 일정일 때, 해당 기간에 속하는 일정 찾기
		List<Schedule> schedulesWithRecurrence = scheduleRepository.findSchedulesWithRecurrenceForRange(
			rangeStartDate, rangeEndDate, calendarId);

		List<Schedule> allSchedules = Stream.concat(
			schedulesInRange.stream(),
			schedulesWithRecurrence.stream().flatMap(s -> s.createSchedulesFromRecurrence().stream())
		).toList();

		// 기간 내 일정 필터링
		List<Schedule> filteredSchedules = allSchedules.stream()
			.filter(s -> !s.getEnd().toLocalDate().isBefore(rangeStartDate) && !s.getStart()
				.toLocalDate()
				.isAfter(rangeEndDate))
			.collect(Collectors.toList());

		// 5. 정렬하는 로직 수행
		filteredSchedules.sort((s1, s2) -> {
			// 1. 날짜별 정렬
			if (!s1.getStart().toLocalDate().equals(s2.getStart().toLocalDate()))
				return s1.getStart().toLocalDate().compareTo(s2.getStart().toLocalDate());

			// 2. 시작 시간별 정렬
			if (!s1.getStart().toLocalTime().equals(s2.getStart().toLocalTime()))
				return s1.getStart().toLocalTime().compareTo(s2.getStart().toLocalTime());

			// 3. 생성 시간별 정렬
			return s1.getCreatedAt().compareTo(s2.getCreatedAt());
		});

		return ScheduleResponseDto.SchedulesInRange.from(filteredSchedules);
	}

	@Override
	@Transactional
	public void deleteSchedule(Member member, Long calendarId, Long scheduleId, String date,
		ScheduleRequestDto.Scope scope) {
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		if (!calendar.canEdit(member.getId())) {
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);
		}

		Schedule schedule = scheduleRepository.findScheduleByIdFetchJoin(scheduleId)
			.orElseThrow(() -> new ApiException(ResponseStatus.SCHEDULE_NOT_FOUND));

		if (!schedule.getCalendar().getId().equals(calendarId)) {
			throw new ApiException(ResponseStatus.SCHEDULE_NOT_FOUND);
		}

		switch (scope) {
			case ALL -> {
				if (schedule.getRecurrence() != null) {
					recurrenceRepository.delete(schedule.getRecurrence());
				}
				scheduleRepository.delete(schedule);
			}
			case ONLY_THIS -> {
				if (schedule.getRecurrence() == null) {
					scheduleRepository.delete(schedule);
				} else {
					schedule.getRecurrence().addException(date);
					scheduleRepository.save(schedule);
				}
			}
			case THIS_AND_FUTURE -> {
				if (schedule.getRecurrence() == null) {
					scheduleRepository.delete(schedule);
				} else {
					schedule.getRecurrence().changeRecurrenceEndDate(date);
				}
			}
		}
	}

	@Override
	@Transactional
	public void shareSchedule(Member member, Long calendarId, Long targetCalendarId,
		List<ScheduleRequestDto.ScheduleTime> scheduleTimes) {
		// 캘린더 존재여부 확인(fromCalendar, toCalendar)
		Calendar fromCalendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));
		Calendar toCalendar = calendarRepository.findByIdWithParticipants(targetCalendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		// 멤버 권한 확인(fromCalendar - participant이면 ok, toCalendar - participant 이면서 edit 이상)
		fromCalendar.validateParticipant(member.getId());
		toCalendar.validateParticipant(member.getId());

		if (!toCalendar.canEdit(member.getId()))
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);

		String nicknameByToCalendar = toCalendar.findParticipant(member.getId())
			.map(Participant::getNickname)
			.orElseThrow(() -> new ApiException(ResponseStatus.INVALID_CALENDAR_PARTICIPATION));

		// toCalendar에 새로운 일정 생성
		List<Schedule> schedules = scheduleTimes.stream().map(st -> {
			if (st.getStartDateTime().isAfter(st.getEndDateTime()))
				throw new ApiException(ResponseStatus.INVALID_SCHEDULE_RANGE);

			LocalDate startDate = st.getStartDateTime().toLocalDate();
			LocalDate endDate = st.getEndDateTime().toLocalDate();

			LocalTime startTime = st.getStartDateTime().toLocalTime();
			LocalTime endTime = st.getEndDateTime().toLocalTime();

			DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

			LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
			LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

			// isAllDay 계산 로직
			boolean isAllDay = LocalTime.MIDNIGHT.equals(startTime)
				&& LocalTime.of(23, 59, 59).equals(endTime);

			Category category = categoryRepository.findByName("default")
				.orElseThrow(() -> new ApiException(ResponseStatus.CATEGORY_NOT_FOUND));

			return Schedule.create(
				nicknameByToCalendar,
				isAllDay,
				startDateTime,
				endDateTime,
				null,
				null,
				null,
				category,
				member,
				toCalendar,
				null
			);
		}).toList();

		schedules.forEach(s -> scheduleRepository.save(s));
	}

	@Override
	public ScheduleResponseDto.SearchList searchSchedules(Member member, String keyword) {
		if (keyword.isBlank()) {
			return new ScheduleResponseDto.SearchList(Collections.emptyList());
		}

		// 키워드를 기반으로 내가 속해있는 캘린더의 모든 일정 검색
		List<Schedule> myScheduleList = scheduleRepository.searchByMemberIdAndKeywordPrefix(member.getId(), keyword);
		return ScheduleResponseDto.SearchList.from(myScheduleList);
	}

}