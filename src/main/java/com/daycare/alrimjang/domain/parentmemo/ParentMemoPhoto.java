package com.daycare.alrimjang.domain.parentmemo;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "parent_memo_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class ParentMemoPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_memo_id", nullable = false)
    private ParentMemo parentMemo;
}