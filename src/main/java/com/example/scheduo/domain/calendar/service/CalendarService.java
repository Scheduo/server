package com.example.scheduo.domain.calendar.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.entity.ParticipationStatus;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.calendar.repository.ParticipantRepository;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.repository.MemberRepository;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CalendarService {

	private final CalendarRepository calendarRepository;
	private final ParticipantRepository participantRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public Calendar createCalendar(CalendarRequestDto.Create request, Long memberId) {
		Member owner = memberRepository.findById(memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.NOT_FOUND_MEMBER));

		Calendar calendar = Calendar.builder()
			.name(request.getTitle())
			.member(owner)
			.build();
		calendarRepository.save(calendar);

		List<CalendarRequestDto.Participant> participants = request.getParticipants();
		if (participants != null) {
			for (CalendarRequestDto.Participant p : participants) {
				Member participantMember = memberRepository.findById(p.getMemberId())
					.orElseThrow(() -> new ApiException(ResponseStatus.NOT_FOUND_MEMBER));

				Participant participant = Participant.builder()
					.member(participantMember)
					.calendar(calendar)
					.nickname(participantMember.getNickname())
					.role(p.getRole())
					.status(ParticipationStatus.PENDING)
					.build();

				participantRepository.save(participant);
			}
		}

		return calendar;
	}
}
