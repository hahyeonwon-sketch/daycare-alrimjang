package com.daycare.alrimjang.domain.notice;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoticeReplyRepository extends JpaRepository<NoticeReply, Long> {

    // 알림장 ID로 답장 목록 조회
    List<NoticeReply> findByNoticeIdOrderByCreatedAtAsc(Long noticeId);

    // 알림장 ID로 답장 목록 조회 + author 함께 로딩 (N+1 방지)
    @EntityGraph(attributePaths = {"author"})
    List<NoticeReply> findWithAuthorByNoticeIdOrderByCreatedAtAsc(Long noticeId);
}