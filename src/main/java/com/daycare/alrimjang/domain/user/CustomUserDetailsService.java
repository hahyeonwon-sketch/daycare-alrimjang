package com.daycare.alrimjang.domain.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 이메일로 유저 조회, 없으면 예외 발생
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일입니다: " + email));

        // PENDING, REJECTED, INACTIVE 상태면 로그인 차단
        if (user.getStatus() != User.Status.ACTIVE) {
            throw new UsernameNotFoundException("승인되지 않은 계정입니다.");
        }

        // Spring Security가 인식하는 권한 형식으로 변환 (ROLE_ADMIN, ROLE_TEACHER, ROLE_PARENT)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}