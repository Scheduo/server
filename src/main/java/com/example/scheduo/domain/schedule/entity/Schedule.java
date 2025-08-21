package com.example.scheduo.domain.schedule.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.common.BaseEntity;
import com.example.scheduo.domain.member.entity.Member;

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

	@ManyToOne(fetch = FetchType.LAZY)
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
	public List<Schedule> createSchedulesFromRecurrence() {
		if (recurrence == null) {
			return List.of(this);
		}
		Period duration = Period.between(startDate, endDate);

		return recurrence.createRecurDates(startDate).stream()
			.map(date -> Schedule.builder()
				.id(id)
				.title(title)
				.isAllDay(isAllDay)
				.startDate(date)
				.endDate(date.plus(duration))
				.startTime(startTime)
				.endTime(endTime)
				.location(location)
				.memo(memo)
				.notificationTime(notificationTime)
				.category(category)
				.member(member)
				.calendar(calendar)
				.recurrence(recurrence)
				.build())
			.toList();
	}

	public void update(
		String title,
		boolean isAllDay,
		String startDate,
		String endDate,
		String startTime,
		String endTime,
		String location,
		String memo,
		NotificationTime notificationTime,
		String recurrenceRule,
		String recurrenceEndDate,
		Category category
	) {
		this.title = title;
		this.isAllDay = isAllDay;
		this.startDate = LocalDate.parse(startDate);
		this.endDate = LocalDate.parse(endDate);
		this.startTime = LocalTime.parse(isAllDay ? "00:00" : startTime);
		this.endTime = LocalTime.parse(isAllDay ? "23:59" : endTime);
		this.location = location;
		this.memo = memo;
		this.notificationTime = notificationTime;
		this.category = category;
		if (recurrenceRule != null && recurrenceEndDate != null) {
			this.recurrence.updateRecurrence(recurrenceRule, recurrenceEndDate);
		}
	}

	public boolean includesDate(LocalDate date) {
		List<Schedule> schedules = this.createSchedulesFromRecurrence();
		return schedules.stream().anyMatch(schedule -> schedule.getStartDate().isEqual(date));
	}
}
