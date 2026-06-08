package com.daycare.alrimjang.domain.announcement;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AnnouncementRequestDto {

    private String title;
    private String content;
    private MultipartFile file;
}