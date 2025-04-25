package com.example.scheduo.domain.schedule.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.common.BaseEntity;
import com.example.scheduo.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Schedule extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private UUID repeatId;

	@Column(length = 100)
	private String title;

	@Column(length = 100)
	private String location;

	private String memo;

	private boolean isAllDay;

	private LocalDate startDate;

	private LocalDate endDate;

	private LocalTime startTime;

	private LocalTime endTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "categoryId")
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "calendarId")
	private Calendar calendar;
}
