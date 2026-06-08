package com.daycare.alrimjang.domain.child;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChildRepository extends JpaRepository<Child, Long> {

    // 반 ID로 원아 목록 조회
    List<Child> findByClassroomId(Long classroomId);

    // 학부모 ID로 원아 조회 (N:M)
    @EntityGraph(attributePaths = {"classroom"})
    @Query("SELECT c FROM Child c JOIN c.parents p WHERE p.id = :parentId")
    Optional<Child> findByParentId(@Param("parentId") Long parentId);

    // 전체 조회 - classroom, parents 같이 로딩
    @Override
    @EntityGraph(attributePaths = {"classroom", "parents"})
    List<Child> findAll();
}