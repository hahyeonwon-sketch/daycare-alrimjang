// NoticeController.java
// 변경: saveNotice, saveDraft에 @Valid 추가
package com.daycare.alrimjang.domain.notice;

import jakarta.validation.Valid;                 // ✅ 추가
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

    @GetMapping("/{id}")
    public String noticeDetail(@AuthenticationPrincipal UserDetails userDetails,
                               @PathVariable Long id, Model model) {
        Notice notice = noticeService.getNoticeDetailForTeacher(userDetails.getUsername(), id);
        model.addAttribute("notice", notice);
        return "teacher/notice-detail";
    }

    @PostMapping("/save")
    public String saveNotice(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute NoticeRequestDto dto) throws IOException {  // ✅ @Valid 추가

        noticeService.saveNotice(userDetails.getUsername(), dto);
        return "redirect:/teacher/notice";
    }

    @PostMapping("/draft")
    public String saveDraft(@AuthenticationPrincipal UserDetails userDetails,
                            @Valid @ModelAttribute NoticeRequestDto dto) throws IOException {  // ✅ @Valid 추가

        noticeService.saveDraft(userDetails.getUsername(), dto);
        return "redirect:/teacher/notice";
    }

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