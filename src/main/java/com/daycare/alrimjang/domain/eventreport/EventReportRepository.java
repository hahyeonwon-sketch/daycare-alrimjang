package com.daycare.alrimjang.domain.eventreport;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventReportRepository extends JpaRepository<EventReport, Long> {

    // 반 ID로 행사보고 목록 조회 (최신순) - photos 같이 로딩
    @EntityGraph(attributePaths = {"photos"})
    List<EventReport> findByClassroomIdOrderByCreatedAtDesc(Long classroomId);

    // 상세 조회 - photos 같이 로딩
    @EntityGraph(attributePaths = {"photos"})
    Optional<EventReport> findWithPhotosById(Long id);

    // 제목 또는 내용으로 검색
    @Query("SELECT e FROM EventReport e WHERE e.classroom.id = :classroomId " +
            "AND (e.title LIKE %:keyword% OR e.content LIKE %:keyword%) " +
            "ORDER BY e.createdAt DESC")
    List<EventReport> searchByKeyword(@Param("classroomId") Long classroomId,
                                      @Param("keyword") String keyword);

    // 기간별 조회
    @Query("SELECT e FROM EventReport e WHERE e.classroom.id = :classroomId " +
            "AND e.eventDate >= :startDate AND e.eventDate <= :endDate " +
            "ORDER BY e.createdAt DESC")
    List<EventReport> findByClassroomIdAndDateBetween(
            @Param("classroomId") Long classroomId,
            @Param("startDate") java.time.LocalDate startDate,
            @Param("endDate") java.time.LocalDate endDate);
}