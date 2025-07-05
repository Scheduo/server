package com.example.scheduo.domain.schedule.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

	@ManyToOne(fetch = FetchType.LAZY)
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn(name = "calendarId")
	private Calendar calendar;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "recurrenceId")
	private Recurrence recurrence;

	public static Schedule create(
		String title,
		boolean isAllDay,
		String startDate,
		String endDate,
		String startTime,
		String endTime,
		String location,
		String memo,
		NotificationTime notificationTime,
		Category category,
		Member member,
		Calendar calendar,
		Recurrence recurrence
	) {

		return Schedule.builder()
			.title(title)
			.isAllDay(isAllDay)
			.startDate(LocalDate.parse(startDate))
			.endDate(LocalDate.parse(endDate))
			.startTime(LocalTime.parse(isAllDay ? "00:00" : startTime))
			.endTime(LocalTime.parse(isAllDay ? "23:59" : endTime))
			.location(location)
			.memo(memo)
			.notificationTime(notificationTime)
			.category(category)
			.member(member)
			.calendar(calendar)
			.recurrence(recurrence)
			.build();
	}

	// TODO: 각각 스케쥴 별로 create recurrence 함수 호출
	public static List<Schedule> createSchedulesFromRecurrence(List<Schedule> schedulesWithRecurrence) {
		// 다 돌면서 일정 생성 해줘야함
		return null;
	}
}
