package com.example.scheduo.global.config.security.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.scheduo.global.config.security.provider.JwtProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
	private final JwtProvider jwtProvider;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) throws IOException {
		// OAuth2 인증 성공 후 처리 로직을 여기에 작성합니다.
		// 예를 들어, JWT 토큰을 생성하고 클라이언트에게 전달하는 등의 작업을 수행할 수 있습니다.

		OAuth2User user = (OAuth2User)authentication.getPrincipal();
		Long memberId = (Long)user.getAttributes().get("memberId");

		String accessToken = jwtProvider.createAccessToken(memberId);
		String refreshToken = jwtProvider.createRefreshToken(memberId);

		String redirectUrl =
			"http://localhost:3000/oauth2/redirect?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
		response.sendRedirect(redirectUrl);
	}

}
