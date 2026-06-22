package com.daycare.alrimjang.global;

import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Profile("prod")
@Component
@RequiredArgsConstructor
public class ProdAdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (userRepository.existsByEmail("admin@daycare.com")) {
            return;
        }

        String adminPassword = System.getenv("ADMIN_PASSWORD");
        if (adminPassword == null || adminPassword.isBlank()) {
            log.error("[ProdAdminInitializer] 환경변수 ADMIN_PASSWORD가 설정되지 않았습니다. Admin 계정 생성을 건너뜁니다.");
            return;
        }

        userRepository.save(User.builder()
                .name("원장")
                .email("admin@daycare.com")
                .password(passwordEncoder.encode(adminPassword))
                .role(User.Role.ADMIN)
                .status(User.Status.ACTIVE)
                .emailNotification(true)
                .build());

        log.info("[ProdAdminInitializer] Admin 계정 생성 완료");
    }
}