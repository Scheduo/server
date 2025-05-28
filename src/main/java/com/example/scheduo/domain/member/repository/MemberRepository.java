package com.example.scheduo.domain.member.repository;

import java.util.List;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	boolean existsByNicknameAndIdNot(String nickname, Long memberId);

	// TODO: like%와 전문 검색 성능 테스트 해보기
	List<Member> findByEmailStartingWith(String emailPrefix);
	Optional<Member> findMemberByEmail(String email);

	Optional<Member> findMemberByEmailAndSocialType(String email, SocialType socialType);
}
