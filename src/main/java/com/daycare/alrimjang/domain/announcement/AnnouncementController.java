package com.daycare.alrimjang.domain.announcement;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    // 교사 공지사항 목록
    @GetMapping("/teacher/announcement")
    public String teacherList(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        List<Announcement> announcements = announcementService.getTeacherAnnouncementList(userDetails.getUsername());
        model.addAttribute("announcements", announcements);
        return "teacher/announcement";
    }

    // 교사 공지사항 상세
    @GetMapping("/teacher/announcement/{id}")
    public String teacherDetail(@PathVariable Long id, Model model) {
        model.addAttribute("announcement", announcementService.getAnnouncement(id));
        return "teacher/announcement-detail";
    }

    // 공지사항 등록
    @PostMapping("/teacher/announcement/save")
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @Valid @ModelAttribute AnnouncementRequestDto dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "제목과 내용을 올바르게 입력해주세요.");
            return "redirect:/teacher/announcement";
        }
        announcementService.saveAnnouncement(userDetails.getUsername(), dto);
        return "redirect:/teacher/announcement";
    }

    // 공지사항 수정
    @PostMapping("/teacher/announcement/{id}/update")
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         @Valid @ModelAttribute AnnouncementRequestDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "제목과 내용을 올바르게 입력해주세요.");
            return "redirect:/teacher/announcement/" + id;
        }
        announcementService.updateAnnouncement(userDetails.getUsername(), id, dto);
        return "redirect:/teacher/announcement";
    }

    // 공지사항 삭제
    @PostMapping("/teacher/announcement/{id}/delete")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id) {
        announcementService.deleteAnnouncement(userDetails.getUsername(), id);
        return "redirect:/teacher/announcement";
    }

    // 학부모 공지사항 목록
    @GetMapping("/parent/announcement")
    public String parentList(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        List<Announcement> announcements = announcementService.getParentAnnouncementList(userDetails.getUsername());
        model.addAttribute("announcements", announcements);
        return "parent/announcement";
    }

    // 학부모 공지사항 상세
    @GetMapping("/parent/announcement/{id}")
    public String parentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("announcement", announcementService.getAnnouncement(id));
        return "parent/announcement-detail";
    }

    // 교사 공지사항 검색
    @GetMapping("/teacher/announcement/search")
    public String teacherSearch(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String keyword,
                                Model model) {
        model.addAttribute("announcements", announcementService.searchTeacherAnnouncement(userDetails.getUsername(), keyword));
        model.addAttribute("keyword", keyword);
        return "teacher/announcement";
    }

    // 학부모 공지사항 검색
    @GetMapping("/parent/announcement/search")
    public String parentSearch(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String keyword,
                               Model model) {
        model.addAttribute("announcements", announcementService.searchParentAnnouncement(userDetails.getUsername(), keyword));
        model.addAttribute("keyword", keyword);
        return "parent/announcement";
    }

    // 교사 - 기간별 조회
    @GetMapping("/teacher/announcement/date")
    public String teacherByDate(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                Model model) {
        model.addAttribute("announcements", announcementService.getTeacherAnnouncementByDate(
                userDetails.getUsername(), startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "teacher/announcement";
    }
}