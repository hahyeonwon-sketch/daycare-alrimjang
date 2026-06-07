package com.daycare.alrimjang.domain.child;

import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "children")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Child {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private User parent;

    public void assignParent(User parent) {
        this.parent = parent;
    }

    public void updateClassroom(Classroom classroom) {
        this.classroom = classroom;
    }
}