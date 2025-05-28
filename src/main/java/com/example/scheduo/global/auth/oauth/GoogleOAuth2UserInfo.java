package com.example.scheduo.global.auth.oauth;

import java.util.Map;

import com.example.scheduo.domain.member.entity.SocialType;

public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attributes;

	public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getEmail() {
		return (String)attributes.get("email");
	}

	@Override
	public String getName() {
		return (String)attributes.get("name");
	}

	@Override
	public SocialType getSocialType() {
		return SocialType.GOOGLE;
	}
}
