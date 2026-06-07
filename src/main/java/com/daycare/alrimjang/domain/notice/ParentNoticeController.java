package com.daycare.alrimjang.domain.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/parent/notice")
@RequiredArgsConstructor
public class ParentNoticeController {

    private final NoticeService noticeService;

    // 학부모 알림장 목록 페이지
    @GetMapping
    public String noticeList(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {

        List<Notice> notices = noticeService.getParentNoticeList(userDetails.getUsername());
        model.addAttribute("notices", notices);
        return "parent/notice";
    }

    // 알림장 상세 조회 + 읽음 처리
    @GetMapping("/{noticeId}")
    public String noticeDetail(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long noticeId,
                               Model model) {

        Notice notice = noticeService.getNoticeDetail(userDetails.getUsername(), noticeId);
        model.addAttribute("notice", notice);
        return "parent/notice-detail";
    }

    // 답장 저장
    @PostMapping("/{noticeId}/reply")
    public String saveReply(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long noticeId,
                            @RequestParam String content) {

        noticeService.saveReply(userDetails.getUsername(), noticeId, content);
        return "redirect:/parent/notice/" + noticeId;
    }
}