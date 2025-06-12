package com.example.scheduo.domain.calendar.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.example.scheduo.global.event.CalendarInvitationAcceptedEvent;
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
	public void inviteMember(Long calendarId, Member inviter, Long inviteeId) {
		Calendar calendar = calendarRepository.findByIdWithParticipants(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		if (!calendar.isOwner(inviter.getId())) {
			throw new ApiException(ResponseStatus.MEMBER_NOT_OWNER);
		}

		Member invitee = memberRepository.findById(inviteeId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));

		Optional<Participant> existingParticipant = calendar.findParticipant(inviteeId);

		if (existingParticipant.isPresent()) {
			existingParticipant.get().reinvite();
			applicationEventPublisher.publishEvent(
				CalendarInvitationEvent.builder()
					.calendarId(calendarId)
					.calendarName(calendar.getName())
					.invitee(invitee)
					.inviterName(calendar.getOwner().getNickname())
					.build());
			return;
		}

		Participant participant = Participant.builder()
			.calendar(calendar)
			.nickname(invitee.getNickname())
			.member(invitee)
			.status(ParticipationStatus.PENDING)
			.role(Role.VIEW)
			.build();

		calendar.addParticipant(participant);

		applicationEventPublisher.publishEvent(
			CalendarInvitationEvent.builder()
				.calendarId(calendarId)
				.calendarName(calendar.getName())
				.invitee(invitee)
				.inviterName(calendar.getOwner().getNickname())
				.build());
	}

	@Override
	@Transactional
	public void acceptInvitation(Long calendarId, Member member) {
		Participant participant = participantRepository.findByCalendarIdAndMemberId(calendarId, member.getId())
			.orElseThrow(() -> new ApiException(ResponseStatus.INVITATION_NOT_FOUND));

		participant.accept();

		applicationEventPublisher.publishEvent(
			CalendarInvitationAcceptedEvent.builder().invitee(member).calendarId(calendarId).build());
	}

	@Override
	@Transactional
	public void rejectInvitation(Long calendarId, Member member) {
		Participant participant = participantRepository.findByCalendarIdAndMemberId(calendarId, member.getId())
			.orElseThrow(() -> new ApiException(ResponseStatus.INVITATION_NOT_FOUND));

		participant.decline();
	}

	@Override
	@Transactional
	public CalendarResponseDto.CalendarInfo createCalendar(CalendarRequestDto.Create request, Member owner) {
		Calendar calendar = Calendar.builder()
			.name(request.getTitle())
			.participants(new ArrayList<>())
			.schedules(new ArrayList<>())
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

	@Override
	@Transactional
	public void editCalendar(CalendarRequestDto.Edit editInfo, Long calendarId, Member member) {
		Calendar calendar = calendarRepository.findById(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		Participant participant = participantRepository.findByCalendarIdAndMemberId(calendarId, member.getId())
			.orElseThrow(() -> new ApiException(ResponseStatus.INVALID_CALENDAR_PARTICIPATION));

		if (editInfo.getTitle() != null) {
			if (participant.getRole() != Role.OWNER) {
				throw new ApiException(ResponseStatus.MEMBER_NOT_OWNER);
			}
			calendar.updateTitle(editInfo.getTitle());
		}

		if (editInfo.getNickname() != null) {
			if (participant.getStatus() != ParticipationStatus.ACCEPTED) {
				throw new ApiException(ResponseStatus.MEMBER_NOT_ACCEPT);
			}
			participant.updateNickname(editInfo.getNickname());
		}
	}

	@Override
	@Transactional
	public void deleteCalendar(Long calendarId, Member owner) {
		Calendar calendar = calendarRepository.findById(calendarId)
			.orElseThrow(() -> new ApiException(ResponseStatus.CALENDAR_NOT_FOUND));

		if (!calendar.isOwner(owner.getId())) {
			throw new ApiException(ResponseStatus.MEMBER_NOT_OWNER);
		}

		calendarRepository.deleteById(calendarId);
	}

	@Override
	@Transactional
	public CalendarResponseDto.CalendarInfoList getCalendars(Member member) {
		List<Calendar> calendars = participantRepository.findCalendarsByMemberId(member.getId());

		return CalendarResponseDto.CalendarInfoList.from(calendars);
	}
}
