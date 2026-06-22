// NoticeRepository.java
// 변경: 반 단위 날짜별 일괄 조회 쿼리 추가 (getNoticeList N+1 방지)
package com.daycare.alrimjang.domain.notice;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    List<Notice> findByChild_Classroom_IdAndDate(Long classroomId, LocalDate date);

    List<Notice> findByChildIdOrderByDateDesc(Long childId);

    // ✅ 반 전체 원아의 특정 날짜 알림장을 한 번에 조회
    @Query("SELECT n FROM Notice n WHERE n.child.classroom.id = :classroomId AND n.date = :date")
    List<Notice> findAllByClassroomIdAndDate(@Param("classroomId") Long classroomId, @Param("date") LocalDate date);

    List<Notice> findAllByChildIdAndDate(Long childId, LocalDate date);

    List<Notice> findByChildIdAndStatusOrderByDateDesc(Long childId, Notice.Status status);

    default Optional<Notice> findByChildIdAndDate(Long childId, LocalDate date) {
        List<Notice> list = findAllByChildIdAndDate(childId, date);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(list.size() - 1));
    }

    @EntityGraph(attributePaths = {"photos"})
    Optional<Notice> findWithDetailsById(Long id);
}