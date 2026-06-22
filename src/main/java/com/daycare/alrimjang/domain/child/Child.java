package com.daycare.alrimjang.domain.child;

import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    // 1:1 → N:M 으로 변경 (엄마/아빠 둘 다 연결 가능)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "child_parents",
            joinColumns = @JoinColumn(name = "child_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_id")
    )
    @Builder.Default
    private List<User> parents = new ArrayList<>();

    public void addParent(User parent) {
        this.parents.add(parent);
    }

    public void updateClassroom(Classroom classroom) {
        this.classroom = classroom;
    }
}