package com.daycare.alrimjang.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login"; // templates/auth/login.html
    }

    // 회원가입 페이지 (학부모용)
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register"; // templates/auth/register.html
    }
}