package com.example.scheduo.global.auth.oauth;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import com.example.scheduo.global.utils.CookieUtils;
import com.nimbusds.oauth2.sdk.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements
	AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
	public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
	public static final String REDIRECT_URI_COOKIE_NAME = "oauth2_redirect_uri";
	private static final int COOKIE_EXPIRE_SECONDS = 180;

	@Value("${app.oauth2.authorized-redirect-uris}")
	private List<String> authorizedRedirectUris;

	@Override
	public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
		return CookieUtils.getCookies(request, REDIRECT_URI_COOKIE_NAME)
			.map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
			.orElse(null);
	}

	@Override
	public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
		HttpServletResponse response) {
		if (authorizationRequest == null) {
			removeAuthorizationRequestCookies(request, response);
			return;
		}

		CookieUtils.addCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
			CookieUtils.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);
		String redirectUri = request.getHeader("Origin");
		if (redirectUri != null && !redirectUri.isEmpty()) {
			if (isAuthorizedRedirectUri(redirectUri)) {
				CookieUtils.addCookie(request, response, REDIRECT_URI_COOKIE_NAME, redirectUri, COOKIE_EXPIRE_SECONDS);
			} else {
				// 허용되지 않은 URI일 경우 예외 처리 또는 로깅
				// throw new IllegalArgumentException("Unauthorized Redirect URI");
			}
		}

		String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_COOKIE_NAME);
		if (StringUtils.isNotBlank(redirectUriAfterLogin)) {
			CookieUtils.addCookie(request, response, REDIRECT_URI_COOKIE_NAME, redirectUriAfterLogin,
				COOKIE_EXPIRE_SECONDS);
		}
	}

	@Override
	public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
		HttpServletResponse response) {
		return this.loadAuthorizationRequest(request);
	}

	public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
		CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
		CookieUtils.deleteCookie(request, response, REDIRECT_URI_COOKIE_NAME);
	}

	private boolean isAuthorizedRedirectUri(String uri) {
		URI clientRedirectUri = URI.create(uri);
		return authorizedRedirectUris.stream()
			.anyMatch(authorizedUri -> {
				URI authorizedURI = URI.create(authorizedUri);
				return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
					&& authorizedURI.getPort() == clientRedirectUri.getPort();
			});
	}
}
