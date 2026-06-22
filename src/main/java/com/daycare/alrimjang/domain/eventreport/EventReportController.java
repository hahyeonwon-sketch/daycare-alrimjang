package com.daycare.alrimjang.domain.eventreport;

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
public class EventReportController {

    private final EventReportService eventReportService;

    // 교사 행사보고 목록
    @GetMapping("/teacher/event")
    public String teacherList(@AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        List<EventReport> eventReports = eventReportService.getTeacherEventReportList(userDetails.getUsername());
        model.addAttribute("eventReports", eventReports);
        return "teacher/event";
    }

    // 교사 행사보고 상세
    @GetMapping("/teacher/event/{id}")
    public String teacherDetail(@PathVariable Long id, Model model) {
        model.addAttribute("eventReport", eventReportService.getEventReport(id));
        return "teacher/event-detail";
    }

    // 행사보고 등록
    @PostMapping("/teacher/event/save")
    public String save(@AuthenticationPrincipal UserDetails userDetails,
                       @Valid @ModelAttribute EventReportRequestDto dto,
                       BindingResult bindingResult,
                       RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "제목, 날짜, 내용을 올바르게 입력해주세요.");
            return "redirect:/teacher/event";
        }
        eventReportService.saveEventReport(userDetails.getUsername(), dto);
        return "redirect:/teacher/event";
    }

    // 행사보고 수정
    @PostMapping("/teacher/event/{id}/update")
    public String update(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id,
                         @Valid @ModelAttribute EventReportRequestDto dto,
                         BindingResult bindingResult,
                         RedirectAttributes redirectAttributes) throws IOException {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "제목, 날짜, 내용을 올바르게 입력해주세요.");
            return "redirect:/teacher/event/" + id;
        }
        eventReportService.updateEventReport(userDetails.getUsername(), id, dto);
        return "redirect:/teacher/event";
    }

    // 행사보고 삭제
    @PostMapping("/teacher/event/{id}/delete")
    public String delete(@AuthenticationPrincipal UserDetails userDetails,
                         @PathVariable Long id) {
        eventReportService.deleteEventReport(userDetails.getUsername(), id);
        return "redirect:/teacher/event";
    }

    // 학부모 행사보고 목록
    @GetMapping("/parent/event")
    public String parentList(@AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        List<EventReport> eventReports = eventReportService.getParentEventReportList(userDetails.getUsername());
        model.addAttribute("eventReports", eventReports);
        return "parent/event";
    }

    // 학부모 행사보고 상세
    @GetMapping("/parent/event/{id}")
    public String parentDetail(@PathVariable Long id, Model model) {
        model.addAttribute("eventReport", eventReportService.getEventReport(id));
        return "parent/event-detail";
    }

    // 교사 행사보고 검색
    @GetMapping("/teacher/event/search")
    public String teacherSearch(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam String keyword,
                                Model model) {
        model.addAttribute("eventReports", eventReportService.searchTeacherEventReport(userDetails.getUsername(), keyword));
        model.addAttribute("keyword", keyword);
        return "teacher/event";
    }

    // 학부모 행사보고 검색
    @GetMapping("/parent/event/search")
    public String parentSearch(@AuthenticationPrincipal UserDetails userDetails,
                               @RequestParam String keyword,
                               Model model) {
        model.addAttribute("eventReports", eventReportService.searchParentEventReport(userDetails.getUsername(), keyword));
        model.addAttribute("keyword", keyword);
        return "parent/event";
    }

    // 교사 - 기간별 조회
    @GetMapping("/teacher/event/date")
    public String teacherByDate(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                Model model) {
        model.addAttribute("eventReports", eventReportService.getTeacherEventReportByDate(
                userDetails.getUsername(), startDate, endDate));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        return "teacher/event";
    }
}