package com.daycare.alrimjang.domain.notification;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 알림 API 응답 전용 DTO.
 * Notification 엔티티를 직접 직렬화하면 연관된 User까지 포함돼
 * 비밀번호 해시 등 민감 정보가 노출될 수 있으므로 필요한 필드만 담는다.
 */
@Getter
public class NotificationResponseDto {

    private final Long id;
    private final String message;
    private final boolean read;
    private final LocalDateTime createdAt;

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.read = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }
}