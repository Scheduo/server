package com.example.scheduo.domain.member.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;
import com.example.scheduo.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {
	private final MemberRepository memberRepository;
	private final CalendarRepository calendarRepository;

	@Transactional
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

		Calendar defaultCalendar = Calendar.builder()
			.member(member)
			.name("기본 캘린더")
			.build();
		calendarRepository.save(defaultCalendar);

		return member;

	}
}