package com.example.scheduo.domain.calendar.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.dto.CalendarRequestDto;
import com.example.scheduo.domain.calendar.dto.CalendarResponseDto;
import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.entity.ParticipationStatus;
import com.example.scheduo.domain.calendar.entity.Role;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.calendar.repository.ParticipantRepository;
import com.example.scheduo.domain.calendar.service.CalendarService;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.repository.MemberRepository;
import com.example.scheduo.global.event.CalendarInvitationEvent;
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
	private final ApplicationEventPublisher applicationEventPublisher;

	@Override
	@Transactional
	public void inviteMember(Long calendarId, Long inviterId, Long inviteeId) {
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		//Todo 멤버 삭제에 따른 수정 필요
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
						applicationEventPublisher.publishEvent(
							CalendarInvitationEvent.builder()
								.calendarId(calendarId)
								.calendarName(calendar.getName())
								.invitee(invitee)
								.inviterName(calendar.getMember().getNickname())
								.build());
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
		// TODO: 추후에 calendar.getMember().getId()를 AuthenticationContext에서 Member 가져와서 사용하도록 수정
		applicationEventPublisher.publishEvent(
			CalendarInvitationEvent.builder()
				.calendarId(calendarId)
				.calendarName(calendar.getName())
				.invitee(invitee)
				.inviterName(calendar.getMember().getNickname())
				.build());
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

	@Override
	@Transactional
	public void rejectInvitation(Long calendarId, Long memberId) {
		Participant participant = participantRepository.findByCalendarIdAndMemberId(calendarId, memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.INVITATION_NOT_FOUND));

		switch (participant.getStatus()) {
			case PENDING -> participant.setStatus(ParticipationStatus.DECLINED);
			case ACCEPTED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_ACCEPTED);
			case DECLINED -> throw new ApiException(ResponseStatus.INVITATION_ALREADY_DECLINED);
		}
	}

	@Override
	@Transactional
	public CalendarResponseDto.CalendarInfo createCalendar(CalendarRequestDto.Create request, Long memberId) {
		Member owner = memberRepository.findById(memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));

		Calendar calendar = Calendar.builder()
			.name(request.getTitle())
			.participants(new ArrayList<>())
			.build();

		Participant ownerParticipant = Participant.builder()
			.member(owner)
			.nickname(owner.getNickname())
			.role(Role.OWNER)
			.status(ParticipationStatus.ACCEPTED)
			.build();

		calendar.addParticipant(ownerParticipant);

		List<CalendarRequestDto.Participant> requestParticipants = request.getParticipants();

		if (requestParticipants != null && !requestParticipants.isEmpty()) {
			List<Long> participantIds = requestParticipants.stream()
				.map(CalendarRequestDto.Participant::getMemberId)
				.toList();

			List<Member> members = memberRepository.findAllById(participantIds);
			Map<Long, Member> memberMap = members.stream()
				.collect(Collectors.toMap(Member::getId, Function.identity()));

			for (CalendarRequestDto.Participant p : requestParticipants) {
				Member participantMember = memberMap.get(p.getMemberId());
				if (participantMember == null) {
					throw new ApiException(ResponseStatus.MEMBER_NOT_FOUND);
				}

				Participant participant = Participant.builder()
					.member(participantMember)
					.nickname(participantMember.getNickname())
					.role(p.getRole())
					.status(ParticipationStatus.PENDING)
					.build();

				calendar.addParticipant(participant);
			}
		}
		calendarRepository.save(calendar);

		return CalendarResponseDto.CalendarInfo.from(calendar);
	}
}
