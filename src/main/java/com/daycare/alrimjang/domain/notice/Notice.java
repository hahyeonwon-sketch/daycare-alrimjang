package com.daycare.alrimjang.domain.notice;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Notice {

    public enum Status { DRAFT, PUBLISHED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    private String meal;
    private String play;
    private String toilet;
    private String special;
    private String extra;
    private String nap;      // 낮잠


    @Column(nullable = false)
    private boolean attended;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.PUBLISHED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "child_id", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NoticePhoto> photos = new ArrayList<>();

    @OneToMany(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<NoticeReply> replies = new ArrayList<>();

    @OneToOne(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true)
    private NoticeRead noticeRead;

    public void update(String meal, String play, String toilet, String special, String extra, boolean attended, String nap) {
        this.meal = meal;
        this.play = play;
        this.toilet = toilet;
        this.special = special;
        this.extra = extra;
        this.attended = attended;
        this.nap = nap;
    }

    public void updateAttended(boolean attended) {
        this.attended = attended;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void updateExtra(String extra) {
        this.extra = extra;
    }
}