package com.example.scheduo.domain.member.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.entity.Participant;
import com.example.scheduo.domain.calendar.entity.ParticipationStatus;
import com.example.scheduo.domain.calendar.entity.Role;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;
import com.example.scheduo.domain.member.repository.MemberRepository;
import com.example.scheduo.domain.member.service.MemberService;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;
	private final CalendarRepository calendarRepository;

	@Override
	public MemberResponseDto.MemberInfo getMyProfile(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));
		return MemberResponseDto.MemberInfo.from(member);
	}

	@Override
	@Transactional
	public MemberResponseDto.MemberInfo editMyProfile(Long memberId, MemberRequestDto.EditInfo editInfo) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));

		if (memberRepository.existsByNicknameAndIdNot(editInfo.getNickname(), memberId)) {
			throw new ApiException(ResponseStatus.DUPLICATE_NICKNAME);
		}

		member.changeNickname(editInfo.getNickname());

		return MemberResponseDto.MemberInfo.from(member);
	}

	@Override
	public MemberResponseDto.MemberList searchByEmail(String emailPrefix) {
		List<Member> members = memberRepository.findByEmailStartingWith(emailPrefix);
		return MemberResponseDto.MemberList.from(members);
	}

	@Transactional
	@Override
	public Member findOrCreateMember(String email, String nickname, SocialType socialType) {
		Optional<Member> existingMember = memberRepository.findMemberByEmailAndSocialType(email, socialType);
		if (existingMember.isPresent()) {
			return existingMember.get();
		}

		Member member = Member.builder()
			.email(email)
			.nickname(nickname)
			.socialType(socialType)
			.build();
		memberRepository.save(member);

		//Todo 멤버 삭제에 따른 수정 필요
		Calendar defaultCalendar = Calendar.builder()
			.name("기본")
			.participants(new ArrayList<>())
			.schedules(new ArrayList<>())
			.build();
		calendarRepository.save(defaultCalendar);
		Participant participant = Participant.builder()
			.role(Role.OWNER)
			.member(member)
			.calendar(defaultCalendar)
			.nickname(member.getNickname())
			.status(ParticipationStatus.ACCEPTED)
			.build();
		defaultCalendar.addParticipant(participant);

		return member;

	}

	@Override
	public Member findById(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));
		return member;
	}
}
