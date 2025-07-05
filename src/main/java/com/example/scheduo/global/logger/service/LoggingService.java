package com.example.scheduo.global.logger.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LoggingService {
	void logRequest(HttpServletRequest request);
	void logResponse(HttpServletRequest request, HttpServletResponse response, long responseTime);
	void logError(HttpServletRequest request, String errorCode, String errorMessage);
}
