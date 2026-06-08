package com.daycare.alrimjang.domain.user;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.classroom.ClassroomRepository;
import com.daycare.alrimjang.domain.classroom.ClassroomTeacher;
import com.daycare.alrimjang.domain.classroom.ClassroomTeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClassroomRepository classroomRepository;
    private final ClassroomTeacherRepository classroomTeacherRepository;
    private final ChildRepository childRepository;

    // 관리자 대시보드
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("pendingCount", userRepository.countByStatus(User.Status.PENDING));
        return "admin/dashboard";
    }

    // 학부모 승인 목록
    @GetMapping("/parent")
    public String parentList(Model model) {
        model.addAttribute("pendingParents", userRepository.findByStatusAndRole(User.Status.PENDING, User.Role.PARENT));
        model.addAttribute("activeParents", userRepository.findByStatusAndRole(User.Status.ACTIVE, User.Role.PARENT));
        return "admin/parent";
    }

    // 학부모 승인
    @PostMapping("/parent/{id}/approve")
    public String approveParent(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.updateStatus(User.Status.ACTIVE);
        userRepository.save(user);
        return "redirect:/admin/parent";
    }

    // 학부모 거절
    @PostMapping("/parent/{id}/reject")
    public String rejectParent(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        user.updateStatus(User.Status.REJECTED);
        userRepository.save(user);
        return "redirect:/admin/parent";
    }

    // 반 관리 목록
    @GetMapping("/classroom")
    public String classroomList(Model model) {
        model.addAttribute("classrooms", classroomRepository.findAll());
        return "admin/classroom";
    }

    // 반 추가
    @PostMapping("/classroom/save")
    public String saveClassroom(@RequestParam String name) {
        classroomRepository.save(Classroom.builder().name(name).build());
        return "redirect:/admin/classroom";
    }

    // 반 삭제
    @PostMapping("/classroom/{id}/delete")
    public String deleteClassroom(@PathVariable Long id) {
        classroomRepository.deleteById(id);
        return "redirect:/admin/classroom";
    }

    // 교사 관리 목록
    @GetMapping("/teacher")
    public String teacherList(Model model) {
        model.addAttribute("teachers", userRepository.findByRole(User.Role.TEACHER));
        model.addAttribute("classrooms", classroomRepository.findAll());
        return "admin/teacher";
    }

    // 교사 계정 생성
    @PostMapping("/teacher/save")
    public String saveTeacher(@RequestParam String name,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam Long classroomId) {
        if (userRepository.existsByEmail(email)) {
            return "redirect:/admin/teacher?error=duplicate";
        }

        User teacher = userRepository.save(User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(User.Role.TEACHER)
                .status(User.Status.ACTIVE)
                .emailNotification(true)
                .build());

        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다."));

        classroomTeacherRepository.save(ClassroomTeacher.builder()
                .classroom(classroom)
                .user(teacher)
                .build());

        return "redirect:/admin/teacher";
    }

    // 교사 삭제
    @PostMapping("/teacher/{id}/delete")
    public String deleteTeacher(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/teacher";
    }

    // 원아 관리 목록
    @GetMapping("/child")
    public String childList(Model model) {
        List<Child> children = childRepository.findAll();
        model.addAttribute("children", children);
        model.addAttribute("classrooms", classroomRepository.findAll());
        model.addAttribute("activeParents", userRepository.findByStatusAndRole(User.Status.ACTIVE, User.Role.PARENT));
        return "admin/child";
    }

    // 원아 등록
    @PostMapping("/child/save")
    public String saveChild(@RequestParam String name,
                            @RequestParam Long classroomId,
                            @RequestParam(required = false) Long parentId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 반입니다."));

        Child child = Child.builder()
                .name(name)
                .classroom(classroom)
                .build();

        childRepository.save(child);

        if (parentId != null) {
            User parent = userRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));
            child.addParent(parent);
            childRepository.save(child);
        }

        return "redirect:/admin/child";
    }

    // 원아 삭제
    @PostMapping("/child/{id}/delete")
    public String deleteChild(@PathVariable Long id) {
        childRepository.deleteById(id);
        return "redirect:/admin/child";
    }

    // 원아에 학부모 연결
    @PostMapping("/child/{id}/link")
    public String linkParent(@PathVariable Long id,
                             @RequestParam Long parentId) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원아입니다."));
        User parent = userRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));
        child.addParent(parent);
        childRepository.save(child);
        return "redirect:/admin/child";
    }
}