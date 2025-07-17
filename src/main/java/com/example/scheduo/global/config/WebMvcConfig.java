package com.example.scheduo.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.scheduo.global.auth.interceptor.AuthInterceptor;
import com.example.scheduo.global.auth.resolver.RequestMemberArgumentResolver;
import com.example.scheduo.global.logger.interceptor.LoggingInterceptor;
import com.example.scheduo.global.utils.AuthExcludedUris;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;
	private final RequestMemberArgumentResolver requestMemberArgumentResolver;
	private final LoggingInterceptor loggingInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns(AuthExcludedUris.ALL);

		registry.addInterceptor(loggingInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns("/health", "/actuator/**", "/v3/api-docs/**", "/swagger-ui/**",
				"/swagger-resources/**")
			.order(1);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(requestMemberArgumentResolver);
	}
}
