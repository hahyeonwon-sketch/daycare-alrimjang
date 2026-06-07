package com.daycare.alrimjang.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    private boolean emailNotification = true;

    private String requestedChildName;
    private String requestedClassName;

    public enum Role {
        ADMIN, TEACHER, PARENT
    }

    public enum Status {
        PENDING, ACTIVE, REJECTED, INACTIVE
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updateEmailNotification(boolean emailNotification) {
        this.emailNotification = emailNotification;
    }

    public void updatePassword(String password) {
        this.password = password;
    }
}