package com.example.scheduo.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;
import com.example.scheduo.domain.calendar.entity.Calendar;
import com.example.scheduo.domain.calendar.repository.CalendarRepository;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;
import com.example.scheduo.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

public interface MemberService {
	MemberResponseDto.GetProfile getMyProfile(Long memberId);
	MemberResponseDto.GetProfile editMyProfile(Long memberId, MemberRequestDto.EditInfo dto);
	MemberResponseDto.SearchProfiles searchByEmail(String email);
}
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