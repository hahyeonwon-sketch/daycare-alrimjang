package com.daycare.alrimjang.domain.announcement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    // 반 ID로 공지사항 목록 조회 (최신순)
    List<Announcement> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    // 제목 또는 내용으로 검색
    @Query("SELECT a FROM Announcement a WHERE a.classroom.id = :classroomId " +
            "AND (a.title LIKE %:keyword% OR a.content LIKE %:keyword%) " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> searchByKeyword(@Param("classroomId") Long classroomId,
                                       @Param("keyword") String keyword);

    // 기간별 조회
    @Query("SELECT a FROM Announcement a WHERE a.classroom.id = :classroomId " +
            "AND a.createdAt >= :startDate AND a.createdAt <= :endDate " +
            "ORDER BY a.createdAt DESC")
    List<Announcement> findByClassroomIdAndDateBetween(
            @Param("classroomId") Long classroomId,
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate);
}