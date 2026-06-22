package com.daycare.alrimjang.domain.notice;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.parentmemo.ParentMemo;
import com.daycare.alrimjang.domain.parentmemo.ParentMemoService;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/parent/notice")
@RequiredArgsConstructor
public class ParentNoticeController {

    private final NoticeService noticeService;
    private final ParentMemoService parentMemoService;
    private final UserRepository userRepository;
    private final ChildRepository childRepository;

    @GetMapping
    public String noticeMain(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam(required = false)
                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {

        if (date == null) date = LocalDate.now();

        User parent = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Child child = childRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("연결된 원아가 없습니다."));

        NoticeService.NoticeDetailResult result =
                noticeService.getNoticeDetailByDate(userDetails.getUsername(), child.getId(), date);
        ParentMemo memo = parentMemoService.getMemo(child.getId(), date);

        model.addAttribute("notice", result.notice());
        model.addAttribute("replies", result.replies());
        model.addAttribute("memo", memo);
        model.addAttribute("selectedDate", date);
        model.addAttribute("childName", child.getName());

        return "parent/notice-detail";
    }

    @GetMapping("/list")
    public String noticeList(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {

        List<Notice> notices = noticeService.getParentNoticeList(userDetails.getUsername());
        model.addAttribute("notices", notices);
        return "parent/notice";
    }

    @PostMapping("/memo")
    public String saveMemo(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                           @RequestParam String content,
                           @RequestParam(required = false) MultipartFile[] photos) throws IOException {

        parentMemoService.saveMemo(userDetails.getUsername(), date, content, photos);
        return "redirect:/parent/notice?date=" + date + "&saved=true";
    }

    @PostMapping("/{noticeId}/reply")
    public String saveReply(@AuthenticationPrincipal UserDetails userDetails,
                            @PathVariable Long noticeId,
                            @RequestParam String content) {

        noticeService.saveReply(userDetails.getUsername(), noticeId, content);
        LocalDate date = noticeService.getNoticeDateById(noticeId);
        return "redirect:/parent/notice?date=" + date;
    }
}