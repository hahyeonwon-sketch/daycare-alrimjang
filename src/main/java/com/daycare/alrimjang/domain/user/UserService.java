package com.daycare.alrimjang.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 학부모 회원가입
    @Transactional
    public void register(RegisterRequestDto dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(User.Role.PARENT)
                .status(User.Status.PENDING)
                .emailNotification(true)
                .requestedClassName(dto.getRequestedClassName())
                .requestedChildName(dto.getRequestedChildName())
                .build();

        userRepository.save(user);
    }

    // 내 정보 조회
    @Transactional(readOnly = true)
    public User getMyInfo(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    // 비밀번호 변경
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword,
                               PasswordEncoder passwordEncoder) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    // 알림 설정 변경
    @Transactional
    public void updateEmailNotification(String email, boolean emailNotification) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.updateEmailNotification(emailNotification);
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(String email, String password, PasswordEncoder passwordEncoder) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        user.updateStatus(User.Status.INACTIVE);
    }
}