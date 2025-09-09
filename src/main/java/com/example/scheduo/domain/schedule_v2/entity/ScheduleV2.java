package com.example.scheduo.domain.schedule_v2.entity;

import java.time.LocalDateTime;

import com.example.scheduo.domain.common.BaseEntity;
import com.example.scheduo.domain.schedule.entity.Category;
import com.example.scheduo.domain.schedule.entity.NotificationTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "schedule_v2")
public class ScheduleV2 extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "calendar_id", nullable = false)
	private Long calendarId;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@Column(name = "serial_id", nullable = false, length = 100)
	private String serialId;

	@Column(length = 100, nullable = false)
	private String title;

	@Column(length = 100)
	private String location;

	@Column(length = 100)
	private String memo;

	@Column(name = "is_all_day", nullable = false)
	private boolean isAllDay;

	private LocalDateTime start;

	private LocalDateTime end;

	@Enumerated(EnumType.STRING)
	@Column(name = "notification_time")
	private NotificationTime notificationTime;
}
