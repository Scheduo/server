package com.example.scheduo.domain.member.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.calendar.entity.Calendar;
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
	public MemberResponseDto.GetProfile getMyProfile(Long memberId) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));
		return MemberResponseDto.GetProfile.from(member);
	}

	@Override
	@Transactional
	public MemberResponseDto.GetProfile editMyProfile(Long memberId, MemberRequestDto.EditInfo editInfo) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new ApiException(ResponseStatus.MEMBER_NOT_FOUND));

		if (memberRepository.existsByNicknameAndIdNot(editInfo.getNickname(), memberId)) {
			throw new ApiException(ResponseStatus.DUPLICATE_NICKNAME);
		}

		member.changeNickname(editInfo.getNickname());

		return MemberResponseDto.GetProfile.from(member);
	}

	@Override
	public MemberResponseDto.SearchProfiles searchByEmail(String emailPrefix) {
		List<Member> members = memberRepository.findByEmailStartingWith(emailPrefix);
		return MemberResponseDto.SearchProfiles.from(members);
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

		Calendar defaultCalendar = Calendar.builder()
			.member(member)
			.name("기본 캘린더")
			.build();
		calendarRepository.save(defaultCalendar);

		return member;

	}
}
