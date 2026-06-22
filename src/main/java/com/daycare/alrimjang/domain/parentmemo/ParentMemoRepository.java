// ParentMemoRepository.java
// 변경: 반 단위 날짜별 일괄 조회 쿼리 추가
package com.daycare.alrimjang.domain.parentmemo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ParentMemoRepository extends JpaRepository<ParentMemo, Long> {

    @EntityGraph(attributePaths = {"photos"})
    List<ParentMemo> findAllByChildIdAndDate(Long childId, LocalDate date);

    // ✅ 반 전체 원아의 특정 날짜 메모를 한 번에 조회
    @Query("SELECT m FROM ParentMemo m WHERE m.child.classroom.id = :classroomId AND m.date = :date")
    List<ParentMemo> findAllByClassroomIdAndDate(@Param("classroomId") Long classroomId, @Param("date") LocalDate date);
}