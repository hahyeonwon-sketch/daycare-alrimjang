package com.daycare.alrimjang.global.config;

import com.daycare.alrimjang.domain.user.CustomUserDetailsService;
import com.daycare.alrimjang.global.LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final LoginSuccessHandler loginSuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 로그인 없이 접근 가능한 경로
                        .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/images/**").permitAll()
                        // ADMIN Role만 접근 가능 (원장 관리자 페이지)
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // TEACHER Role만 접근 가능 (교사 페이지)
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        // PARENT Role만 접근 가능 (학부모 페이지)
                        .requestMatchers("/parent/**").hasRole("PARENT")
                        .requestMatchers("/", "/auth/**", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        // 그 외 모든 요청은 로그인 필요
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .successHandler(loginSuccessHandler) // 역할별 페이지 분기
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt: 같은 비밀번호도 매번 다른 해시값 생성 → 레인보우 테이블 공격 방어
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // AuthenticationManager: 로그인 시 이메일/비밀번호 검증 담당
        return config.getAuthenticationManager();
    }
}