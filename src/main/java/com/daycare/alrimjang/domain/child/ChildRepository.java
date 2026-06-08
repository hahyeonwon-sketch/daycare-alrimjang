package com.daycare.alrimjang.domain.child;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child, Long> {

    // 반 ID로 원아 목록 조회 (교사 알림장 목록 페이지)
    List<Child> findByClassroomId(Long classroomId);

    // 학부모 ID로 원아 조회 (1:1 관계) - classroom 같이 로딩
    @EntityGraph(attributePaths = {"classroom"})
    Optional<Child> findByParentId(Long parentId);
}