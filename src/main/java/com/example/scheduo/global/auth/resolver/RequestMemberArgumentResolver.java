package com.example.scheduo.global.auth.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.global.auth.RequestMemberHolder;
import com.example.scheduo.global.auth.annotation.RequestMember;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RequestMemberArgumentResolver implements HandlerMethodArgumentResolver {

	private final RequestMemberHolder requestMemberHolder;

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(RequestMember.class)
			&& Member.class.isAssignableFrom(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
		NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		Member member = requestMemberHolder.getMember();
		if (member == null) {
			throw new ApiException(ResponseStatus.MEMBER_NOT_FOUND);
		}
		return member;
	}
}
