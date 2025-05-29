package com.example.scheduo.global.config.security.entry;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {
		String exception = (String) request.getAttribute("exception");
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=UTF-8");

		String message;
		switch (exception) {
			case "INVALID_TOKEN":
				message = "토큰이 유효하지 않습니다.";
				break;
			case "NO_TOKEN":
				message = "토큰이 없습니다.";
				break;
			default:
				message = "인증에 실패하였습니다.";
				break;
		}

		response.getWriter().write(
			String.format("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\"}", message)
		);
	}
}