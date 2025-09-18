package com.example.scheduo.domain.schedule_v2.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Occurrence {
	private final String occurrenceId; // {scheduleId}_{occurrenceStart}
	private final Long scheduleId;
	private final LocalDateTime occurrenceStart;
	private final LocalDateTime occurrenceEnd;
	private final LocalDateTime notificationTime; // 실제 일정 알림 발생 시간
	private final boolean isException;
}
