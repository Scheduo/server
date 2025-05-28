package com.example.scheduo.global.auth.oauth;

import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

public class OAuth2UserInfoFactory {
	public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
		return switch (registrationId.toLowerCase()) {
			case "google" -> new GoogleOAuth2UserInfo(attributes);
			case "kakao" -> new KakaoOAuth2UserInfo(attributes);
			default -> {
				OAuth2Error error = new OAuth2Error(
					"unsupported_provider",
					"지원하지 않는 소셜 로그인입니다: " + registrationId,
					null);
				throw new OAuth2AuthenticationException(error, error.getDescription());
			}
		};
	}
}
