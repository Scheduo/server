package com.example.scheduo.domain.schedule_v2.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.scheduo.domain.schedule.entity.Schedule;

@Service
public class OccurrenceService {

	public List<Occurrence> createOccurrences(Schedule schedule, LocalDateTime start, LocalDateTime end) {
		if (schedule.getRecurrence() == null) {
			return createSingleOccurrence(schedule);
		} else {
			return createRecurringOccurrences(schedule, start, end);
		}
	}

	private List<Occurrence> createSingleOccurrence(Schedule schedule) {
		return List.of(create(schedule));
	}

	private List<Occurrence> createRecurringOccurrences(Schedule schedule, LocalDateTime rangeStart,
		LocalDateTime rangeEnd) {
		List<Schedule> schedules = schedule.createSchedulesFromRecurrence();
		return schedules.stream()
			.filter(s -> !s.getStart().isBefore(rangeStart) && s.getStart().isBefore(rangeEnd))
			.map(this::create)
			.toList();
	}

	private Occurrence create(Schedule schedule) {
		return Occurrence.builder()
			.occurrenceId(generateOccurrenceId(schedule.getId(), schedule.getStart()))
			.scheduleId(schedule.getId())
			.occurrenceStart(schedule.getStart())
			.occurrenceEnd(schedule.getEnd())
			.notificationTime(schedule.getStart().minusMinutes(schedule.getNotificationTime().getMinutes()))
			.isException(false)
			.build();
	}

	private String generateOccurrenceId(Long scheduleId, LocalDateTime occurrenceStart) {
		return scheduleId + "_" + occurrenceStart.toString();
	}
}
