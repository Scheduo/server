package com.example.scheduo.global.logger.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LoggingService {
	void logRequest(HttpServletRequest request, Long memberId);
	void logResponse(HttpServletRequest request, HttpServletResponse response,
		Long memberId, long responseTime);
	void logError(HttpServletRequest request, Long memberId,
		String errorCode, String errorMessage);
}
