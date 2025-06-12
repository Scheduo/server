package com.example.scheduo.domain.schedule.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.common.BaseEntity;
import com.example.scheduo.domain.member.entity.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedule")
public class Schedule extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 100)
	private String title;

	@Column(length = 100)
	private String location;

	private String memo;

	@Column(nullable = false)
	private boolean isAllDay;

	@Column(nullable = false)
	private LocalDate startDate;

	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private LocalTime startTime;

	@Column(nullable = false)
	private LocalTime endTime;

	@Enumerated(EnumType.STRING)
	private NotificationTime notificationTime;

	@Column(nullable = false, columnDefinition = "boolean default false")
	private boolean isOverride;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "categoryId")
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId")
	private Member member;

	@Setter
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendarId")
	private Calendar calendar;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "recurrenceId")
	private Recurrence recurrence;
}
