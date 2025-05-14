package com.example.scheduo.global.auth;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.entity.SocialType;
import com.example.scheduo.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	private final MemberRepository memberRepository;

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		String userNameAttributeName = userRequest.getClientRegistration()
			.getProviderDetails()
			.getUserInfoEndpoint()
			.getUserNameAttributeName();

		String name = (String)oAuth2User.getAttributes().get("name");
		String email = (String)oAuth2User.getAttributes().get("email");
		Optional<Member> memberByEmail = memberRepository.findMemberByEmail(email);
		Member member;
		if (memberByEmail.isEmpty()) {
			member = Member.builder()
				.nickname(name)
				.email(email)
				.socialType(SocialType.GOOGLE)
				.build();
			memberRepository.save(member);
		} else {
			member = memberByEmail.get();
		}

		Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
		attributes.put("memberId", member.getId());

		return new DefaultOAuth2User(
			oAuth2User.getAuthorities(),
			attributes,
			userNameAttributeName
		);
	}
}
