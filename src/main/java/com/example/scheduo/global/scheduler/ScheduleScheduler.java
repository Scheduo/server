package com.example.scheduo.global.scheduler;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.scheduo.domain.schedule.entity.Schedule;
import com.example.scheduo.domain.schedule.repository.ScheduleRepository;
import com.example.scheduo.domain.schedule_v2.model.Occurrence;
import com.example.scheduo.domain.schedule_v2.model.OccurrenceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ScheduleScheduler {
	private final ScheduleRepository scheduleRepository;
	private final OccurrenceService occurrenceService;

	// 예시로 10초마다 실행
	// 실제로는 1시간마다, 또는 30분마다 실행
	@Scheduled(fixedRate = 10000)
	public void checkSchedulesAndSendNotifications() {
		LocalDateTime now = LocalDateTime.now();
		//현재 시간부터 내일 30분 후까지의 일정 조회
		LocalDateTime to = now.plusDays(1).plusMinutes(30);

		List<Schedule> singleSchedules = scheduleRepository.findUpcomingSchedules(now, to);
		List<Schedule> recurringSchedules = scheduleRepository.findUpcomingSchedulesWithRecurrence(now, to);

		List<Occurrence> occurrences = Stream.concat(singleSchedules.stream(), recurringSchedules.stream())
			.flatMap(schedule -> occurrenceService.createOccurrences(schedule, now, to).stream())
			.toList();

		//알림 시간 기준으로 필터링(현재 시간 ~ 30분 후)
		occurrences = occurrences.stream()
			.filter(occurrence -> !occurrence.getNotificationTime().isBefore(now) &&
				!occurrence.getNotificationTime().isAfter(now.plusMinutes(30))
			).toList();

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

		//디버깅용 출력
		for (Occurrence occurrence : occurrences) {
			System.out.println(
				occurrence.getScheduleId() + " 일정이 " + occurrence.getOccurrenceStart().format(formatter) + "에 시작합니다."
					+ "(알림 시간: " + occurrence.getNotificationTime().format(formatter) + ")"
			);
		}

		//여기서 이제 알림 생성
	}
}
