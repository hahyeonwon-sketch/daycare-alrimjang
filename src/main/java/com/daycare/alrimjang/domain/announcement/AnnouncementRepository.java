package com.daycare.alrimjang.domain.announcement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 반 ID로 공지사항 목록 조회 (최신순)
    List<Announcement> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);
}