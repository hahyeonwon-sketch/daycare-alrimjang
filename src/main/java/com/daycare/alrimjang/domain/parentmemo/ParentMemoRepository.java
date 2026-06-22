package com.daycare.alrimjang.domain.parentmemo;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ParentMemoRepository extends JpaRepository<ParentMemo, Long> {

    @EntityGraph(attributePaths = {"photos"})
    List<ParentMemo> findAllByChildIdAndDate(Long childId, LocalDate date);
}