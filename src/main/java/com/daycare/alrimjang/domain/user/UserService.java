package com.daycare.alrimjang.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.daycare.alrimjang.domain.user.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequestDto dto) {

        // 이메일 중복 체크
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 학부모 계정 생성 (status = PENDING, 원장 승인 대기)
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword())) // 비밀번호 암호화
                .phone(dto.getPhone())
                .role(User.Role.PARENT)
                .status(User.Status.PENDING) // 가입 후 원장 승인 대기
                .emailNotification(true)
                .requestedClassName(dto.getRequestedClassName())
                .requestedChildName(dto.getRequestedChildName())
                .build();

        userRepository.save(user);
    }
}