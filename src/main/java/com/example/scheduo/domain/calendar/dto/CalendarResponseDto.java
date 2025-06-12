package com.example.scheduo.domain.calendar.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.entity.ParticipationStatus;
import com.example.scheduo.domain.calendar.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class CalendarResponseDto {

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CalendarInfo {
		private Long calendarId;
		private String title;

		public static CalendarInfo from(Calendar calendar) {
			return CalendarInfo.builder()
				.calendarId(calendar.getId())
				.title(calendar.getName())
				.build();
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CalendarInfoList {
		private List<CalendarInfo> calendars;

		public static CalendarInfoList from(List<Calendar> calendars) {
			List<CalendarInfo> calendarInfoList = calendars.stream()
				.map(CalendarInfo::from)
				.collect(Collectors.toList());
			return CalendarInfoList.builder()
				.calendars(calendarInfoList)
				.build();
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class CalendarDetailInfo {
		private Long calendarId;
		private String title;
		private Role memberRole;
		private String memberNickname;
		private List<ParticipantInfo> participants;

		public static CalendarDetailInfo from(Calendar calendar, Participant myParticipant) {
			List<ParticipantInfo> participantInfoList = calendar.getParticipants().stream()
				.filter(p -> p.getStatus().equals(ParticipationStatus.ACCEPTED))
				.map(ParticipantInfo::from)
				.collect(Collectors.toList());

			return CalendarDetailInfo.builder()
				.calendarId(calendar.getId())
				.title(calendar.getName())
				.memberRole(myParticipant.getRole())
				.memberNickname(myParticipant.getNickname())
				.participants(participantInfoList)
				.build();
		}
	}

	@Builder
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	private static class ParticipantInfo {
		private Long participantId;
		private String nickname;
		private Role role;
		private String email;

		public static ParticipantInfo from(Participant participant) {
			return ParticipantInfo.builder()
				.participantId(participant.getId())
				.nickname(participant.getNickname())
				.role(participant.getRole())
				.email(participant.getMember().getEmail())
				.build();
		}
	}
}
