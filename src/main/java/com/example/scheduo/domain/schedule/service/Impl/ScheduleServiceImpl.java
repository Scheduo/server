package com.example.scheduo.domain.schedule.service.Impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.schedule.dto.ScheduleRequestDto;
import com.example.scheduo.domain.schedule.dto.ScheduleResponseDto;
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

	// TODO: 월별 조회 일정 로직 구현 필요

	// TODO: prefetching(지난달(5) - 이번달(6) - 다음달(7))
		// 캘린더 6월 -> 6.1(월) ~ 6.30(금) -> 5.30(일) ~ 7.1(일)
		// 구현 방법 : """클라 쿼리 3번"""(선택) vs. 서버 3달치 전송
		// 고려사항 : 요청 -> dirty check -> 클라(해시)
	// TODO: 쿼리가 너무 늦음(캐싱, 쿼리 최적화)
	// TODO: 예외 조건 추가(rrule - exception 테이블) (수정 api 선 작업 후 진행)


	// 테스트코드

	/**
	 *
	 *
	 * 단일일정
	 * 반복일정(매일)
	 * 반복일정(매주)
	 * 반복일정(매달)
	 * 반복일정(매년)
	 *
	 * ----------------
	 *
	 * 단일일정(반복x) 조회
	 * 단일일정(반복o) 조회
	 * 기간일정(반복x) 조회
	 * 기간일정(반복o) 조회
	 * 기간일정(2달 걸쳐있는거) 조회
	 *
	 *
	 */

	@Override
	public ScheduleResponseDto.SchedulesByMonthly getSchedulesByMonth(Member member, Long calendarId, String date) {
		/**
		 * 로직 => 월별조회
		 * 1. 캘린더에 멤버가 속해있는지 검증
		 * 2. month에 대한 schedule db 쿼리 날리기
		 * 3. 단일 일정 조회
		 * 4. 반복 일정 조회 -> 직접 일정을 생성
		 * 5. 예외 조건 추가
		 */
		// 캘린더 조회
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		// 멤버가 캘린더에 속해있는지 검증
		if (!calendar.validateParticipant(member.getId()))
			throw new ApiException(ResponseStatus.PARTICIPANT_PERMISSION_LEAK);

		// DATE to MONTH 파싱 로직
		// 1. start || end를 기준으로 가져옴, 여러날 일정을 만듬, 만약 그 달에 속하지 않는다면 거름..?
		int month = LocalDate.parse(date).getMonthValue();
		List<Schedule> schedulesInSingle = scheduleRepository.findSchedulesByStartMonthAndEndMonth(month, calendarId);

		// recurrence에서 enddate를 기준으로 지나지 않은 일정 조회
		LocalDate firstDayOfMonth = LocalDate.parse(date).withDayOfMonth(1);
		LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
		List<Schedule> schedulesWithRecurrence = scheduleRepository.findSchedulesWithRecurrence(firstDayOfMonth,
			lastDayOfMonth, calendarId);

		// recurrence 인스턴스 일정 생성
		List<Schedule> allSchedules = Stream.concat(
			schedulesInSingle.stream(),
			schedulesWithRecurrence.stream().flatMap(s -> s.createSchedulesFromRecurrence().stream())
		).toList();

		List<Schedule> filteredSchedules = new ArrayList<>(allSchedules.stream()
			.filter(s -> s.getStartDate().getMonthValue() == month || s.getEndDate().getMonthValue() == month)
			.toList());

		// filteredSchedules.sort((s1, s2) -> {
		// 	if (s1.getStartDate().equals(s2.getStartDate())) {
		// 		return s1.getStartTime().compareTo(s2.getStartTime());
		// 	}
		// 	return s1.getStartDate().compareTo(s2.getStartDate());
		// });

		return ScheduleResponseDto.SchedulesByMonthly.from(calendarId, filteredSchedules);
	}
}
