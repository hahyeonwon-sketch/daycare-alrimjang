package com.daycare.alrimjang.domain.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    // 교사 ID로 담당 반 목록 조회
    @Query("SELECT ct.classroom FROM ClassroomTeacher ct WHERE ct.user.id = :teacherId")
    List<Classroom> findByTeacherId(@Param("teacherId") Long teacherId);
}