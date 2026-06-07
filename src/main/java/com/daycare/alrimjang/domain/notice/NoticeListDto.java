package com.daycare.alrimjang.domain.notice;

import com.daycare.alrimjang.domain.child.Child;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NoticeListDto {

    private Long childId;
    private String childName;
    private boolean attended;
    private boolean hasNotice;
    private String parentMemoContent;
    private LocalDateTime readAt;
    private String meal;
    private String play;
    private String toilet;
    private String special;
    private String extra;
    private List<String> photoFileNames;

    public static NoticeListDto of(Child child, Notice notice) {
        return NoticeListDto.builder()
                .childId(child.getId())
                .childName(child.getName())
                .attended(notice != null && notice.isAttended())
                .hasNotice(notice != null)
                .readAt(notice != null && notice.getNoticeRead() != null
                        ? notice.getNoticeRead().getReadAt() : null)
                .meal(notice != null ? notice.getMeal() : null)
                .play(notice != null ? notice.getPlay() : null)
                .toilet(notice != null ? notice.getToilet() : null)
                .special(notice != null ? notice.getSpecial() : null)
                .extra(notice != null ? notice.getExtra() : null)
                .photoFileNames(notice != null
                        ? notice.getPhotos().stream().map(NoticePhoto::getFilePath).toList()
                        : List.of())
                .build();
    }
}