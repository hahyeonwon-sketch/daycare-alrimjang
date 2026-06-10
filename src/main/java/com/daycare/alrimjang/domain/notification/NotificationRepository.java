package com.daycare.alrimjang.domain.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<com.daycare.alrimjang.domain.notification.Notification, Long> {

    // 사용자별 알림 목록 조회 (최신순)
    List<com.daycare.alrimjang.domain.notification.Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 읽지 않은 알림 개수
    long countByUserIdAndIsReadFalse(Long userId);
}