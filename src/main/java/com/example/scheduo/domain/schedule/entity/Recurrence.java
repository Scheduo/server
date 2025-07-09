package com.example.scheduo.domain.schedule.entity;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cglib.core.Local;

import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.transform.recurrence.Frequency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recurrence")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recurrence {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TEXT")
	private String recurrenceRule;

	private LocalDate recurrenceEndDate;

	public static Recurrence create(String recurrenceRule, String recurrenceEndDate) {
		LocalDate recurrenceEndLocalDate = LocalDate.parse(recurrenceEndDate);
		RRule<LocalDate> rRule = createRRule(recurrenceRule, recurrenceEndLocalDate);
		return Recurrence.builder()
			.recurrenceRule(rRule.toString())
			.recurrenceEndDate(recurrenceEndLocalDate)
			.build();
	}

	private static RRule<LocalDate> createRRule(String recurrenceRule, LocalDate recurrenceEndDate) {
		Recur.Builder<LocalDate> builder = new Recur.Builder<>();
		Frequency frequency = Frequency.valueOf(recurrenceRule.toUpperCase());
		Recur<LocalDate> recur = builder
			.frequency(frequency)
			.interval(1)
			.until(recurrenceEndDate)
			.build();

		return new RRule<>(recur);
	}

	/**
	 * {
	 *     [
	 *     	{
	 *     	   "startDate": ~~,
	 *	    	"endDate": ~~
	 *     	},
	 *     	~~
	 *     ]
	 * }
	 */
	// TODO: 일정 생성 구현 필요
	public List<RecurDate> createRecurDates(LocalDate startDate, LocalDate endDate) {
		RRule<LocalDate> rrule = new RRule<>(this.recurrenceRule);
		Recur<LocalDate> recur = rrule.getRecur();

		// recur.getDates(
		// 	startDate,
		// 	startDate,
		// 	endDate
		// ).stream()
		// 	.map(date -> ((Date)date)
		// 		.toInstant()
		// 		.atZone(ZoneId.systemDefault())
		// 		.toLocalDate())
		// 	.collect(Collectors.toList());

		return null;
	}

	protected static class RecurDate {
		private final LocalDate startDate;
		private final LocalDate endDate;

		public RecurDate(LocalDate startDate, LocalDate endDate) {
			this.startDate = startDate;
			this.endDate = endDate;
		}

	}
}
