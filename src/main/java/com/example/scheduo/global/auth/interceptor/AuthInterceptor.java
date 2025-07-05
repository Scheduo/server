package com.example.scheduo.global.auth.interceptor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.scheduo.domain.member.entity.Member;
import com.example.scheduo.domain.member.service.MemberService;
import com.example.scheduo.global.auth.dto.RequestMemberHolder;
import com.example.scheduo.global.response.exception.ApiException;
import com.example.scheduo.global.response.status.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

	private final MemberService memberService;
	private final RequestMemberHolder requestMemberHolder;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		Long memberId = (Long)authentication.getPrincipal();

		Member member = memberService.findById(memberId);
		if (member == null) {
			throw new ApiException(ResponseStatus.MEMBER_NOT_FOUND);
		}

		requestMemberHolder.setMember(member);
		return true;
	}
}
