package com.example.scheduo.global.config.security.entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.example.scheduo.global.logger.service.LoggingService;
import com.example.scheduo.global.response.status.ResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final LoggingService loggingService;
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		ResponseStatus exception = (ResponseStatus) request.getAttribute("exception");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		String message;
		switch (exception) {
			case INVALID_TOKEN, NOT_EXIST_TOKEN:
				message = exception.getMessage();
				break;
			default:
				message = ResponseStatus.DEFAULT_TOKEN_ERROR.getMessage();
				break;
		}

		loggingService.logError(request, exception.getStatus(), message);

		Map<String, Object> result = new HashMap<>();
		result.put("code", 401);
		result.put("success", false);
		result.put("message", message);

		ObjectMapper objectMapper = new ObjectMapper();
		response.getWriter().write(objectMapper.writeValueAsString(result));
	}
}