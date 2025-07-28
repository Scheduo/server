package com.example.scheduo.global.config.security.entry;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.example.scheduo.global.logger.service.LoggingService;
import com.example.scheduo.global.response.ApiResponse;
import com.example.scheduo.global.response.exception.ErrorResponseDto;
import com.example.scheduo.global.response.status.ResponseStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final LoggingService loggingService;

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		ResponseStatus exception = (ResponseStatus)request.getAttribute("exception");
		if (exception == null) {
			exception = ResponseStatus.DEFAULT_TOKEN_ERROR;
		}
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		loggingService.logError(request, exception.getStatus(), exception.getMessage());

		ErrorResponseDto result = ApiResponse.onFailure(exception);

		ObjectMapper objectMapper = new ObjectMapper();
		response.getWriter().write(objectMapper.writeValueAsString(result));
	}
}