package com.example.scheduo.global.config.security.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.example.scheduo.global.auth.oauth.HttpCookieOAuth2AuthorizationRequestRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2FailureHandler implements AuthenticationFailureHandler {

	private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException exception) throws IOException, ServletException {
		httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);
		log.error("OAuth2 authentication failed: {}", exception.getMessage());
	}

}
