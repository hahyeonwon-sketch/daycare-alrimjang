package com.daycare.alrimjang.global;

import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // ADMIN 계정이 없을 때만 생성 (중복 생성 방지)
        if (!userRepository.existsByEmail("admin@daycare.com")) {
            User admin = User.builder()
                    .name("원장")
                    .email("admin@daycare.com")
                    .password(passwordEncoder.encode("admin1234")) // 초기 비밀번호
                    .role(User.Role.ADMIN)
                    .status(User.Status.ACTIVE)
                    .emailNotification(true)
                    .build();

            userRepository.save(admin);
            System.out.println("ADMIN 계정 생성 완료: admin@daycare.com / admin1234");
        }
    }
}