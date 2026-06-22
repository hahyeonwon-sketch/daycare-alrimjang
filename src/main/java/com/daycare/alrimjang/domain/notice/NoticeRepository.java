package com.daycare.alrimjang.domain.notice;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 특정 날짜의 반 전체 알림장 조회 (교사 목록 페이지)
    List<Notice> findByChild_Classroom_IdAndDate(Long classroomId, LocalDate date);

    // 학부모용 - 특정 원아 알림장 전체 조회 (최신순)
    List<Notice> findByChildIdOrderByDateDesc(Long childId);

    List<Notice> findAllByChildIdAndDate(Long childId, LocalDate date);

    List<Notice> findByChildIdAndStatusOrderByDateDesc(Long childId, Notice.Status status);

    default Optional<Notice> findByChildIdAndDate(Long childId, LocalDate date) {
        List<Notice> list = findAllByChildIdAndDate(childId, date);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }

    @EntityGraph(attributePaths = {"photos"})
    Optional<Notice> findWithDetailsById(Long id);
}