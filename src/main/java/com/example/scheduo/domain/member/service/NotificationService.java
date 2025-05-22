package com.example.scheduo.domain.member.service;

import com.example.scheduo.domain.member.dto.NotificationResponseDto;

public interface NotificationService {
	NotificationResponseDto.GetNotifications findAllByMemberId(Long memberId);
}
