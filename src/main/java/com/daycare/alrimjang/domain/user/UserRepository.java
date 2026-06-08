package com.daycare.alrimjang.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 역할로 조회
    List<User> findByRole(User.Role role);

    // 상태 + 역할로 조회
    List<User> findByStatusAndRole(User.Status status, User.Role role);

    // 상태로 개수 조회
    long countByStatus(User.Status status);
}