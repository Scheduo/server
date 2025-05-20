package com.example.scheduo.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findMemberByEmail(String email);

	Optional<Member> findMemberByEmailAndSocialType(String email, SocialType socialType);
}
