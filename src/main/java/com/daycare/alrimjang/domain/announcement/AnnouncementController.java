package com.daycare.alrimjang.domain.announcement;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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
                       @ModelAttribute AnnouncementRequestDto dto) throws IOException {
        announcementService.saveAnnouncement(userDetails.getUsername(), dto);
        return "redirect:/teacher/announcement";
    }

    // 공지사항 수정
    @PostMapping("/teacher/announcement/{id}/update")
    public String update(@PathVariable Long id,
                         @ModelAttribute AnnouncementRequestDto dto) throws IOException {
        announcementService.updateAnnouncement(id, dto);
        return "redirect:/teacher/announcement";
    }

    // 공지사항 삭제
    @PostMapping("/teacher/announcement/{id}/delete")
    public String delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
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
}