package com.example.scheduo.domain.calendar.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.entity.ParticipationStatus;
import com.example.scheduo.domain.calendar.entity.Role;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.calendar.repository.ParticipantRepository;
import com.example.scheduo.domain.calendar.service.CalendarService;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.repository.MemberRepository;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarServiceImpl implements CalendarService {
	private final MemberRepository memberRepository;
	private final CalendarRepository calendarRepository;
	private final ParticipantRepository participantRepository;

	@Override
	@Transactional
	public void inviteMember(Long calendarId, Long inviterId, Long inviteeId) {
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		if (!calendar.getMember().getId().equals(inviterId)) {
			throw new ApiException(ResponseStatus.MEMBER_NOT_OWNER);
		}

		Member invitee = memberRepository.findById(inviteeId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));

		for (Participant participant : calendar.getParticipants()) {
			if (participant.getMember().getId().equals(inviteeId)) {
				switch (participant.getStatus()) {
					case PENDING -> throw new ApiException(ResponseStatus.MEMBER_ALREADY_INVITED);
					case ACCEPTED -> throw new ApiException(ResponseStatus.MEMBER_ALREADY_PARTICIPANT);
					case DECLINED -> {
						participant.setStatus(ParticipationStatus.PENDING);
						return;
					}
				}
			}
		}

		Participant participant = Participant.builder()
			.calendar(calendar)
			.member(invitee)
			.status(ParticipationStatus.PENDING)
			.role(Role.VIEW)
			.build();
		calendar.addParticipant(participant);
	}

	@Override
	@Transactional
	public void acceptInvitation(Long calendarId, Long memberId) {
		Participant participant = participantRepository.findByCalendarIdAndMemberId(calendarId, memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.INVITATION_NOT_FOUND));

		switch (participant.getStatus()) {
			case PENDING -> participant.setStatus(ParticipationStatus.ACCEPTED);
			case ACCEPTED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_ACCEPTED);
			case DECLINED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_DECLINED);
		}
	}
}
