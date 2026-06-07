package com.daycare.alrimjang.domain.notice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 특정 날짜의 반 전체 알림장 조회 (교사 목록 페이지)
    List<Notice> findByChild_Classroom_IdAndDate(Long classroomId, LocalDate date);

    // 특정 원아의 특정 날짜 알림장 조회 (중복 방지)
    Optional<Notice> findByChildIdAndDate(Long childId, LocalDate date);

    // 학부모용 - 특정 원아 알림장 전체 조회 (최신순)
    List<Notice> findByChildIdOrderByDateDesc(Long childId);
}