package com.daycare.alrimjang.global;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    // ADMIN 대시보드 (임시)
    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    // TEACHER 메인 (임시)
    @GetMapping("/teacher/notice")
    public String teacherNotice() {
        return "teacher/notice";
    }

    // PARENT 메인 (임시)
    @GetMapping("/parent/notice")
    public String parentNotice() {
        return "parent/notice";
    }
}