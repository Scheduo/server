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

		// OAuth2User user = (OAuth2User)authentication.getPrincipal();
		// Long memberId = (Long)user.getAttributes().get("memberId");
		//
		// String deviceUUID = generateDeviceUUID();
		// String accessToken = jwtProvider.createAccessToken(memberId);
		// String refreshToken = jwtProvider.createRefreshToken(memberId, deviceUUID);

		// refreshTokenService.saveRefreshToken(memberId, deviceUUID, refreshToken, JwtProvider.EXPIRE_REFRESH_MS);

		// String redirectUrl =
		// 	"http://localhost:3000/oauth2/redirect?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
		// response.sendRedirect(redirectUrl);
		String targetUrl = determineTargetUrl(request, response, authentication);
		clearAuthenticationAttributes(request, response);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	@Override
	public String determineTargetUrl(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) {
		// OAuth2 인증 성공 후 리다이렉트할 URL을 결정하는 로직을 여기에 작성합니다.
		// 예를 들어, 클라이언트의 요청에 따라 다른 URL로 리다이렉트할 수 있습니다.
		Optional<String> redirectUri = CookieUtils.getCookies(request,
			HttpCookieOAuth2AuthorizationRequestRepository.REDIRECT_URI_PARAM_COOKIE_NAME).map(
			Cookie::getValue);
		String targetUrl = redirectUri.orElse("http://localhost:3000/oauth2/redirect");

		OAuth2User user = (OAuth2User)authentication.getPrincipal();
		Long memberId = (Long)user.getAttributes().get("memberId");

		String deviceUUID = generateDeviceUUID();
		String accessToken = jwtProvider.createAccessToken(memberId);
		String refreshToken = jwtProvider.createRefreshToken(memberId, deviceUUID);

		refreshTokenService.saveRefreshToken(memberId, deviceUUID, refreshToken, JwtProvider.EXPIRE_REFRESH_MS);

		return UriComponentsBuilder.fromUriString(targetUrl)
			.queryParam("accessToken", accessToken)
			.queryParam("refreshToken", refreshToken)
			.build().toUriString();
	}

	protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
		super.clearAuthenticationAttributes(request);
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
	}

	private String generateDeviceUUID() {
		return UUID.randomUUID().toString();
	}

}
