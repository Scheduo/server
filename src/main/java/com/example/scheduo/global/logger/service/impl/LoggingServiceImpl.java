package com.example.scheduo.global.logger.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.scheduo.global.logger.service.LoggingService;

@Slf4j
@Service
public class LoggingServiceImpl implements LoggingService {

	@Override
	public void logRequest(HttpServletRequest request, Long memberId) {
		String method = request.getMethod();
		String path = request.getRequestURI();

		log.info("Request started - method: {}, path: {}, memberId: {}",
			method, path, memberId);
	}

	@Override
	public void logResponse(HttpServletRequest request, HttpServletResponse response,
		Long memberId, long responseTime) {
		String method = request.getMethod();
		String path = request.getRequestURI();
		int status = response.getStatus();

		log.info("Request completed - method: {}, path: {}, memberId: {}, status: {}, responseTime: {}ms",
			method, path, memberId, status, responseTime);
	}

	@Override
	public void logError(HttpServletRequest request, Long memberId,
		String errorCode, String errorMessage) {
		String method = request.getMethod();
		String path = request.getRequestURI();

		log.error("Request failed - method: {}, path: {}, memberId: {}, errorCode: {}, errorMessage: {}",
			method, path, memberId, errorCode, errorMessage);
	}
}