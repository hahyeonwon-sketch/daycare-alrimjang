package com.daycare.alrimjang.domain.schedule;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 반 ID로 일정 목록 조회
    List<Schedule> findByClassroomId(Long classroomId);

    // 전날 알림 발송용 - 내일 날짜 + 미발송 일정 조회
    List<Schedule> findByDateAndNotifiedFalse(LocalDate date);
}