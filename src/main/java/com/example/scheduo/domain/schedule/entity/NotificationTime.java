package com.example.scheduo.domain.schedule.entity;

import lombok.Getter;

@Getter
public enum NotificationTime {
	ONE_DAY_BEFORE(1440),
	ONE_HOUR_BEFORE(60),
	THIRTY_MINUTES_BEFORE(30),
	FIVE_MINUTES_BEFORE(5);

	private final int minutes;

	NotificationTime(int minutes) {
		this.minutes = minutes;
	}
}
