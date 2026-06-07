package com.daycare.alrimjang.domain.notice;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

    // 알림장 저장
    @PostMapping("/save")
    public String saveNotice(@AuthenticationPrincipal UserDetails userDetails,
                             @ModelAttribute NoticeRequestDto dto) throws IOException {

        noticeService.saveNotice(userDetails.getUsername(), dto);
        return "redirect:/teacher/notice";
    }
}