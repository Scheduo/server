package com.example.scheduo.domain.member.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.scheduo.domain.member.entity.Notification;

@Service
public interface NotificationService {
	List<Notification> findAllByMemberId(Long memberId);
}
