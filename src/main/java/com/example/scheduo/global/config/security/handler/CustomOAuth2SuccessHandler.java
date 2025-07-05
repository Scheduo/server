package com.example.scheduo.global.config.security.handler;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.scheduo.global.auth.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.scheduo.global.auth.service.RefreshTokenService;
import com.example.scheduo.global.config.security.provider.JwtProvider;
import com.example.scheduo.global.utils.CookieUtils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;
	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) throws IOException {
		// OAuth2 인증 성공 후 처리 로직을 여기에 작성합니다.
		// 예를 들어, JWT 토큰을 생성하고 클라이언트에게 전달하는 등의 작업을 수행할 수 있습니다.

		String targetUrl = determineTargetUrl(request, response);

		OAuth2User user = (OAuth2User)authentication.getPrincipal();
		Long memberId = (Long)user.getAttributes().get("memberId");

		String deviceUUID = generateDeviceUUID();
		String accessToken = jwtProvider.createAccessToken(memberId);
		String refreshToken = jwtProvider.createRefreshToken(memberId, deviceUUID);

		refreshTokenService.saveRefreshToken(memberId, deviceUUID, refreshToken, JwtProvider.EXPIRE_REFRESH_MS);

		String redirectUri = UriComponentsBuilder.fromUriString(targetUrl)
			.path("/oauth2/redirect")
			.queryParam("accessToken", accessToken)
			.queryParam("refreshToken", refreshToken)
			.build().toString();
		clearAuthenticationAttributes(request, response);
		getRedirectStrategy().sendRedirect(request, response, redirectUri);
	}

	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		Optional<String> redirectUri = CookieUtils.getCookies(request,
			HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI).map(Cookie::getValue);

		return redirectUri.orElse(getDefaultTargetUrl());
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);
	}

	private String generateDeviceUUID() {
		return UUID.randomUUID().toString();
	}

}
