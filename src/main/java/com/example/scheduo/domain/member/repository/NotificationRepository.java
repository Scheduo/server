package com.example.scheduo.domain.member.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.scheduo.domain.member.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

	List<Notification> findAllByMemberIdAndIsReadFalse(Long memberId);
}