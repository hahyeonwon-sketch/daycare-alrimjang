package com.daycare.alrimjang.domain.notice;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.parentmemo.ParentMemo;
import com.daycare.alrimjang.domain.parentmemo.ParentMemoPhoto;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class NoticeListDto {

    private Long childId;
    private String childName;
    private Long noticeId;
    private boolean attended;
    private boolean hasNotice;
    private String parentMemoContent;
    private List<String> parentMemoPhotos;
    private LocalDateTime readAt;
    private String meal;
    private String play;
    private String toilet;
    private String special;
    private String extra;
    private List<String> photoFileNames;
    private List<String> replyContents;
    private List<String> replyParentNames;
    private Notice.Status status;
    private String nap;
    private String temperature;

    public static NoticeListDto of(Child child, Notice notice, ParentMemo memo) {
        return NoticeListDto.builder()
                .childId(child.getId())
                .childName(child.getName())
                .noticeId(notice != null ? notice.getId() : null)
                .attended(notice != null && notice.isAttended())
                .hasNotice(notice != null)
                .status(notice != null ? notice.getStatus() : null)
                .parentMemoContent(memo != null ? memo.getContent() : null)
                .parentMemoPhotos(memo != null
                        ? memo.getPhotos().stream().map(ParentMemoPhoto::getFilePath).toList()
                        : List.of())
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
                .replyContents(notice != null && notice.getReplies() != null
                        ? notice.getReplies().stream()
                          .filter(r -> r.getAuthor().getRole() == com.daycare.alrimjang.domain.user.User.Role.PARENT)
                          .map(NoticeReply::getContent).toList()
                        : List.of())
                .replyParentNames(notice != null && notice.getReplies() != null
                        ? notice.getReplies().stream()
                          .filter(r -> r.getAuthor().getRole() == com.daycare.alrimjang.domain.user.User.Role.PARENT)
                          .map(r -> r.getAuthor().getName()).toList()
                        : List.of())
                .nap(notice != null ? notice.getNap() : null)
                .build();
    }
}