package com.daycare.alrimjang.domain.schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 교사 일정 페이지
    @GetMapping("/teacher/schedule")
    public String teacherSchedule(@AuthenticationPrincipal UserDetails userDetails,
                                  Model model) {
        List<Schedule> schedules = scheduleService.getScheduleList(userDetails.getUsername());
        model.addAttribute("schedules", schedules);
        return "teacher/schedule";
    }

    // 학부모 일정 페이지
    @GetMapping("/parent/schedule")
    public String parentSchedule(@AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        List<Schedule> schedules = scheduleService.getParentScheduleList(userDetails.getUsername());
        model.addAttribute("schedules", schedules);
        return "parent/schedule";
    }

    // 일정 등록
    @PostMapping("/teacher/schedule/save")
    public String saveSchedule(@AuthenticationPrincipal UserDetails userDetails,
                               @ModelAttribute ScheduleRequestDto dto) {
        scheduleService.saveSchedule(userDetails.getUsername(), dto);
        return "redirect:/teacher/schedule";
    }

    // 일정 수정
    @PostMapping("/teacher/schedule/{id}/update")
    public String updateSchedule(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id,
                                 @ModelAttribute ScheduleRequestDto dto) {
        scheduleService.updateSchedule(userDetails.getUsername(), id, dto);
        return "redirect:/teacher/schedule";
    }

    // 일정 삭제
    @PostMapping("/teacher/schedule/{id}/delete")
    public String deleteSchedule(@AuthenticationPrincipal UserDetails userDetails,
                                 @PathVariable Long id) {
        scheduleService.deleteSchedule(userDetails.getUsername(), id);
        return "redirect:/teacher/schedule";
    }
}