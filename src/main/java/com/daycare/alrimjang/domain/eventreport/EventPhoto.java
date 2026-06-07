package com.daycare.alrimjang.domain.eventreport;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "event_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class EventPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_report_id", nullable = false)
    private EventReport eventReport;
}