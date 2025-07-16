package com.example.scheduo.global.logger.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.global.auth.dto.RequestMemberHolder;
import com.example.scheduo.global.logger.service.LoggingService;

@Component
@RequiredArgsConstructor
public class LoggingInterceptor implements HandlerInterceptor {

	private final LoggingService loggingService;
	private final RequestMemberHolder requestMemberHolder;
	private static final String START_TIME_ATTRIBUTE = "startTime";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
		Object handler) throws Exception {

		long startTime = System.currentTimeMillis();
		request.setAttribute(START_TIME_ATTRIBUTE, startTime);

		Long memberId = extractMemberId();
		if (memberId != null) {
			MDC.put("memberId", String.valueOf(memberId));
		}

		loggingService.logRequest(request);

		return true;
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
		Object handler, Exception ex) throws Exception {

		Long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
		long responseTime = System.currentTimeMillis() - startTime;

		if (ex == null) {
			loggingService.logResponse(request, response, responseTime);
		}
	}

	private Long extractMemberId() {
		try {
			Member member = requestMemberHolder.getMember();
			return member != null ? member.getId() : null;
		} catch (Exception e) {
			return null;
		}
	}
}