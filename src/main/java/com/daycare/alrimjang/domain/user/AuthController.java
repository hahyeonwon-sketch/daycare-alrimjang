package com.daycare.alrimjang.domain.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // 회원가입 페이지
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequestDto", new RegisterRequestDto());
        return "auth/register";
    }

    // 회원가입 처리
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterRequestDto dto,
                           BindingResult bindingResult,
                           Model model) {

        // 유효성 검사 실패 시 회원가입 페이지로 돌아감
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            // 가입 성공 시 로그인 페이지로 이동 + 승인 대기 안내
            return "redirect:/auth/login?registered";
        } catch (IllegalArgumentException e) {
            // 이메일 중복 시 에러 메시지 표시
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        }
    }
}