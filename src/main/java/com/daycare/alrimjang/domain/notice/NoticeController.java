package com.daycare.alrimjang.domain.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/teacher/notice")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    // 교사 알림장 목록 페이지
    @GetMapping
    public String noticeList(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {

        if (date == null) date = LocalDate.now();

        List<NoticeListDto> noticeList = noticeService.getNoticeList(userDetails.getUsername(), date);

        model.addAttribute("noticeList", noticeList);
        model.addAttribute("selectedDate", date);

        return "teacher/notice";
    }

    // 교사 알림장 상세 (답변 보기)
    @GetMapping("/{id}")
    public String noticeDetail(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id, Model model) {
        Notice notice = noticeService.getNoticeDetailForTeacher(userDetails.getUsername(), id);
        model.addAttribute("notice", notice);
        return "teacher/notice-detail";
    }

    // 알림장 저장 (임시저장 / 발행)
    @PostMapping("/save")
    public String saveNotice(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute NoticeRequestDto dto) throws IOException {

        noticeService.saveNotice(userDetails.getUsername(), dto);
        return "redirect:/teacher/notice";
    }

    // 임시저장
    @PostMapping("/draft")
    public String saveDraft(@AuthenticationPrincipal UserDetails userDetails,
                            @ModelAttribute NoticeRequestDto dto) throws IOException {

        noticeService.saveDraft(userDetails.getUsername(), dto);
        return "redirect:/teacher/notice";
    }

    // 등원 여부 즉시 변경
    @PostMapping("/attended")
    @ResponseBody
    public ResponseEntity<?> updateAttended(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long childId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam boolean attended) {

        noticeService.updateAttended(userDetails.getUsername(), childId, date, attended);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/draft/all")
    public String saveAllDraft(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String extra,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        noticeService.saveAllDraft(userDetails.getUsername(), extra, date);
        return "redirect:/teacher/notice?date=" + date;
    }
}