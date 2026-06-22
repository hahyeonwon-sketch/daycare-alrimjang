// NoticeRequestDto.java
// 변경: childId, date에 @NotNull 검증 추가
package com.daycare.alrimjang.domain.notice;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class NoticeRequestDto {

    @NotNull(message = "원아 ID는 필수입니다.")   // ✅ 추가
    private Long childId;

    @NotNull(message = "날짜는 필수입니다.")       // ✅ 추가
    private LocalDate date;

    private String meal;
    private String play;
    private String toilet;
    private String special;
    private String extra;
    private boolean attended;
    private List<MultipartFile> photos;
    private String teacherReply;
    private String nap;
    private String temperature;
}