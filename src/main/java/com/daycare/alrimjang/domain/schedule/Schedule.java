package com.daycare.alrimjang.domain.schedule;

import com.daycare.alrimjang.domain.classroom.Classroom;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDate date;

    private String content;

    @Column(nullable = false)
    private boolean notified = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    public void update(String title, LocalDate date, String content) {
        this.title = title;
        this.date = date;
        this.content = content;
    }

    public void markAsNotified() {
        this.notified = true;
    }

    private LocalDate endDate; // 종료일 추가

    public void update(String title, LocalDate date, LocalDate endDate, String content) {
        this.title = title;
        this.date = date;
        this.endDate = endDate;
        this.content = content;
    }
}