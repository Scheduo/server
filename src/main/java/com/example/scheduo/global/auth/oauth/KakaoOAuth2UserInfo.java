package com.example.scheduo.global.auth.oauth;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.scheduo.domain.member.entity.SocialType;

public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
	private final Map<String, Object> attributes;

	public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String getEmail() {
		Object object = attributes.get("kakao_account");
		LinkedHashMap accountMap = (LinkedHashMap)object;
		return (String)accountMap.get("email");
	}

	@Override
	public String getName() {
		Object object = attributes.get("kakao_account");
		if (object instanceof LinkedHashMap) {
			LinkedHashMap accountMap = (LinkedHashMap)object;
			Object profileObj = accountMap.get("profile");
			if (profileObj instanceof LinkedHashMap) {
				LinkedHashMap profileMap = (LinkedHashMap)profileObj;
				return (String)profileMap.get("nickname"); // ✅ nickname 사용
			}
		}
		return null;
	}

	@Override
	public SocialType getSocialType() {
		return SocialType.KAKAO;
	}
}
