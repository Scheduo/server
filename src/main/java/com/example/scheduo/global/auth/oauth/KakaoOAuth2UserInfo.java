package com.example.scheduo.global.auth.oauth;

import java.util.Map;

import com.example.scheduo.domain.member.entity.SocialType;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attributes;

	public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getEmail() {
		Object kakaoAccount = attributes.get("kakao_account");
		if (!(kakaoAccount instanceof Map<?, ?> accountMap)) {
			return null;
		}
		Object email = accountMap.get("email");
		if (email instanceof String) {
			return (String)email;
		}
		return null;
	}

	@Override
	public String getName() {
		Object kakaoAccount = attributes.get("kakao_account");
		if (!(kakaoAccount instanceof Map<?, ?> accountMap)) {
			return null;
		}

		Object profileObj = accountMap.get("profile");
		if (!(profileObj instanceof Map<?, ?> profileMap)) {
			return null;
		}

		Object nickname = profileMap.get("nickname");
		if (nickname instanceof String) {
			return (String)nickname;
		}

		return null;
	}

	@Override
	public SocialType getSocialType() {
		return SocialType.KAKAO;
	}
}
