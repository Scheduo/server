package com.example.scheduo.global.auth.oauth;

import com.example.scheduo.domain.member.entity.SocialType;

public interface OAuth2UserInfo {
	String getEmail();

	String getName();

	SocialType getSocialType();
}
