package com.daycare.alrimjang.domain.schedule;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class ScheduleRequestDto {

    private String title;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate date;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate; // 종료일 추가

    private String content;
}