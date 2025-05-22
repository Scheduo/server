package com.example.scheduo.domain.member.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.scheduo.domain.member.dto.MemberRequestDto;
import com.example.scheduo.domain.member.dto.MemberResponseDto;
import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.repository.MemberRepository;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{
	private final MemberRepository memberRepository;

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
		Member updatedMember = memberRepository.save(member);

		return MemberResponseDto.GetProfile.from(updatedMember);
	}

	@Override
	public List<MemberResponseDto.GetProfile> searchByEmail(String emailPrefix) {
		List<Member> members = memberRepository.findByEmailStartingWith(emailPrefix);
		return members.stream()
			.map(MemberResponseDto.GetProfile::from)
			.collect(Collectors.toList());
	}
}
