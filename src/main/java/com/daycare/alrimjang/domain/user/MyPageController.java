// MyPageController.java
// 변경: PasswordEncoder 필드·import 제거, changePassword/withdraw 호출부 파라미터 수정
package com.daycare.alrimjang.domain.user;

import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.classroom.ClassroomRepository;
import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MyPageController {

    private final UserService userService;
    // ✅ PasswordEncoder 필드 제거
    private final ClassroomRepository classroomRepository;
    private final ChildRepository childRepository;

    @GetMapping("/teacher/mypage")
    public String teacherMyPage(@AuthenticationPrincipal UserDetails userDetails,
                                Model model) {
        User user = userService.getMyInfo(userDetails.getUsername());
        List<Classroom> classrooms = classroomRepository.findByTeacherId(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("classrooms", classrooms);
        return "teacher/mypage";
    }

    @GetMapping("/parent/mypage")
    public String parentMyPage(@AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User user = userService.getMyInfo(userDetails.getUsername());
        Child child = childRepository.findByParentId(user.getId()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("child", child);
        return "parent/mypage";
    }

    @PostMapping("/mypage/password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 RedirectAttributes redirectAttributes) {
        try {
            // ✅ passwordEncoder 파라미터 제거
            userService.changePassword(userDetails.getUsername(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "비밀번호가 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        return role.equals("ROLE_TEACHER") ? "redirect:/teacher/mypage" : "redirect:/parent/mypage";
    }

    @PostMapping("/mypage/notification")
    public String updateNotification(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestParam(defaultValue = "false") boolean emailNotification,
                                     RedirectAttributes redirectAttributes) {
        userService.updateEmailNotification(userDetails.getUsername(), emailNotification);
        redirectAttributes.addFlashAttribute("successMessage", "알림 설정이 변경되었습니다.");

        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        return role.equals("ROLE_TEACHER") ? "redirect:/teacher/mypage" : "redirect:/parent/mypage";
    }

    @PostMapping("/mypage/withdraw")
    public String withdraw(@AuthenticationPrincipal UserDetails userDetails,
                           @RequestParam String password,
                           RedirectAttributes redirectAttributes) {
        try {
            // ✅ passwordEncoder 파라미터 제거
            userService.withdraw(userDetails.getUsername(), password);
            SecurityContextHolder.clearContext();
            return "redirect:/auth/login?withdraw";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            String role = userDetails.getAuthorities().iterator().next().getAuthority();
            return role.equals("ROLE_TEACHER") ? "redirect:/teacher/mypage" : "redirect:/parent/mypage";
        }
    }
}