// ScheduleRepository.java
// 변경: findByDateAndNotifiedFalse → JOIN FETCH로 classroom 한 번에 로딩 (N+1 방지)
package com.daycare.alrimjang.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByClassroomId(Long classroomId);

    // ✅ JOIN FETCH로 classroom 즉시 로딩 → 루프 안 LAZY 쿼리 제거
    @Query("SELECT s FROM Schedule s JOIN FETCH s.classroom WHERE s.date = :date AND s.notified = false")
    List<Schedule> findByDateAndNotifiedFalse(@Param("date") LocalDate date);
}