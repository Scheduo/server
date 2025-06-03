package com.example.scheduo.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.scheduo.global.auth.AuthInterceptor;
import com.example.scheduo.global.auth.RequestMemberArgumentResolver;
import com.example.scheduo.global.utils.AuthExcludedUris;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

	private final AuthInterceptor authInterceptor;
	private final RequestMemberArgumentResolver requestMemberArgumentResolver;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authInterceptor)
			.addPathPatterns("/**")
			.excludePathPatterns(AuthExcludedUris.ALL);
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(requestMemberArgumentResolver);
	}
}
