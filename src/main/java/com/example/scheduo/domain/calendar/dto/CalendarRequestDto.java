package com.example.scheduo.domain.calendar.dto;

import java.util.List;

import com.example.scheduo.domain.calendar.entity.Role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CalendarRequestDto {

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Invite {
		private Long memberId;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Create {
		@NotBlank(message = "캘린더 제목은 필수입니다.")
		private String title;

		@Valid
		private List<Participant> participants;
	}

	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Participant {
		@NotNull(message = "memberId는 필수입니다.")
		private Long memberId;
		private Role role;
	}
}
