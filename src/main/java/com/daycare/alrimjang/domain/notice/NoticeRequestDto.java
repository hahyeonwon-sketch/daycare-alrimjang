package com.daycare.alrimjang.domain.notice;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class NoticeRequestDto {

    private Long childId;
    private LocalDate date;
    private String meal;
    private String play;
    private String toilet;
    private String special;
    private String extra;
    private boolean attended;
    private List<MultipartFile> photos; // 사진 추가
}