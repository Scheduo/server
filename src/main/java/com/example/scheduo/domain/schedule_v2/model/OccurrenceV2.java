package com.example.scheduo.domain.schedule_v2.model;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OccurrenceV2 {
	private final String occurrenceId; // {scheduleId}_{occurrenceStart}
	private final Long scheduleId;
	private final LocalDateTime occurrenceStart;
	private final LocalDateTime occurrenceEnd;
	private final String notificationTime;
	private final boolean isException;
}
