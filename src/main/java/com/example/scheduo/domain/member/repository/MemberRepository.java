package com.example.scheduo.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findMemberByEmail(String email);

	Optional<Member> findMemberByEmailAndSocialType(String email, SocialType socialType);
}
