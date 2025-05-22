package com.example.scheduo.global.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.service.MemberService;
import com.example.scheduo.global.auth.oauth.OAuth2UserInfo;
import com.example.scheduo.global.auth.oauth.OAuth2UserInfoFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	private final MemberService memberService;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oAuth2User = super.loadUser(userRequest);
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId,
			oAuth2User.getAttributes());

		Member member = memberService.findOrCreateMember(
			oAuth2UserInfo.getEmail(),
			oAuth2UserInfo.getName(),
			oAuth2UserInfo.getSocialType()
		);

		Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
		attributes.put("memberId", member.getId());

		return new DefaultOAuth2User(
			oAuth2User.getAuthorities(),
			attributes,
			userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName()
		);
	}
}
