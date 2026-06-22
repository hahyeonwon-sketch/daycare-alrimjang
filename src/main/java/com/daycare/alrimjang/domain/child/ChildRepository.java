// ChildRepository.java
// 변경: findByClassroomId에 @EntityGraph(parents) 추가 → 알림 발송 루프 N+1 방지
package com.daycare.alrimjang.domain.child;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child, Long> {

    // ✅ parents 함께 로딩 (ScheduleNotificationService·NoticeService 루프 N+1 방지)
    @EntityGraph(attributePaths = {"parents"})
    List<Child> findByClassroomId(Long classroomId);

    @EntityGraph(attributePaths = {"classroom"})
    @Query("SELECT c FROM Child c JOIN c.parents p WHERE p.id = :parentId")
    Optional<Child> findByParentId(@Param("parentId") Long parentId);

    @Override
    @EntityGraph(attributePaths = {"classroom", "parents"})
    List<Child> findAll();
}