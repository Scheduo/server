package com.example.scheduo.global.logger.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoggingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
		FilterChain chain) throws IOException, ServletException {

		// TraceId 생성 및 설정
		String traceId = generateTraceId();
		MDC.put("traceId", traceId);

		try {
			chain.doFilter(request, response);
		} finally {
			MDC.clear();
		}
	}

	private String generateTraceId() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
	}
}