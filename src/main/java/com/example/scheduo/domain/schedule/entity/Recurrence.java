package com.example.scheduo.domain.schedule.entity;

import java.time.LocalDate;
import java.util.List;

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

	public List<LocalDate> createRecurDates(LocalDate startDate) {
		String recurrenceRule = this.recurrenceRule.replaceFirst("^RRULE:\\s*", "").trim();
		RRule<LocalDate> rrule = new RRule<>(recurrenceRule);
		Recur<LocalDate> recur = rrule.getRecur();

		return recur.getDates(startDate, this.recurrenceEndDate);
	}

	public String getFrequency() {
		return this.recurrenceRule.split(":")[1].split(";")[0].split("=")[1];
	}
}
