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
	public void logRequest(HttpServletRequest request) {
		String method = request.getMethod();
		String path = request.getRequestURI();

		log.info("🚀 REQUEST START | {} {}",
			method, path);
	}

	@Override
	public void logResponse(HttpServletRequest request, HttpServletResponse response, long responseTime) {
		String method = request.getMethod();
		String path = request.getRequestURI();
		int status = response.getStatus();
		String emoji = getStatusEmoji(status);

		log.info("{} REQUEST COMPLETE | {} {} | Status: {} | ResponseTime: {}ms",
			emoji, method, path, status, responseTime);
	}

	@Override
	public void logError(HttpServletRequest request, String errorCode, String errorMessage) {
		String method = request.getMethod();
		String path = request.getRequestURI();

		log.error("❌ REQUEST FAILED | {} {} | Error: {} - {}",
			method, path, errorCode, errorMessage);
	}

	private String getStatusEmoji(int status) {
		if (status >= 200 && status < 300) return "✅";
		if (status >= 300 && status < 400) return "🔄";
		if (status >= 400 && status < 500) return "⚠️";
		if (status >= 500) return "💥";
		return "❓";
	}
}