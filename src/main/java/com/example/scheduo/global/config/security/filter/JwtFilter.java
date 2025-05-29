package com.example.scheduo.global.config.security.filter;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.scheduo.global.config.security.provider.JwtProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
	private final JwtProvider jwtProvider;
	private final AntPathMatcher antPathMatcher;

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String token = resolveToken(httpRequest);

		// 토큰이 없으면 인증 예외 발생
		if (token == null) {
			request.setAttribute("exception", "NO_TOKEN");
			chain.doFilter(request, response);
			return;
		}

		// 토큰이 있지만 유효하지 않으면 예외 발생
		if (!jwtProvider.validateToken(token)) {
			request.setAttribute("exception", "INVALID_TOKEN");
			chain.doFilter(request, response);
			return;
		}

		// 인증 객체 생성 및 저장
		Authentication authentication = jwtProvider.getAuthentication(token);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		// permitAll 경로 목록
		String path = request.getServletPath();
		return antPathMatcher.match("/v3/api-docs/**", path)
			|| antPathMatcher.match("/swagger-ui/**", path)
			|| antPathMatcher.match("/test/**", path);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null;
	}
}