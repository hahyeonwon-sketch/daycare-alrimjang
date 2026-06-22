// UserService.java
// 변경: changePassword, withdraw 파라미터에서 PasswordEncoder 중복 제거
//       (필드 주입으로 이미 있음)
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

    @Transactional(readOnly = true)
    public User getMyInfo(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    // ✅ PasswordEncoder 파라미터 제거 (필드 주입 사용)
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 올바르지 않습니다.");
        }

        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void updateEmailNotification(String email, boolean emailNotification) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        user.updateEmailNotification(emailNotification);
    }

    // ✅ PasswordEncoder 파라미터 제거 (필드 주입 사용)
    @Transactional
    public void withdraw(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        user.updateStatus(User.Status.INACTIVE);
    }
}