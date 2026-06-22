package com.daycare.alrimjang.domain.notice;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.classroom.ClassroomRepository;
import com.daycare.alrimjang.domain.notification.NotificationService;
import com.daycare.alrimjang.domain.parentmemo.ParentMemo;
import com.daycare.alrimjang.domain.parentmemo.ParentMemoRepository;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import com.daycare.alrimjang.global.FileUploadUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final ChildRepository childRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;
    private final NoticePhotoRepository noticePhotoRepository;
    private final NoticeReadRepository noticeReadRepository;
    private final NoticeReplyRepository noticeReplyRepository;
    private final ParentMemoRepository parentMemoRepository;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public record NoticeDetailResult(Notice notice, List<NoticeReply> replies) {}

    // ===== 권한 검증 헬퍼 =====

    private void validateTeacherOwnsChild(User teacher, Child child) {
        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        boolean owns = classrooms.stream()
                .anyMatch(c -> c.getId().equals(child.getClassroom().getId()));
        if (!owns) {
            throw new IllegalArgumentException("담당 반의 원아가 아닙니다.");
        }
    }

    private void validateParentOwnsNotice(User parent, Notice notice) {
        boolean owns = notice.getChild().getParents().stream()
                .anyMatch(p -> p.getId().equals(parent.getId()));
        if (!owns) {
            throw new IllegalArgumentException("본인 자녀의 알림장이 아닙니다.");
        }
    }

    private void validateTeacherOwnsNotice(User teacher, Notice notice) {
        validateTeacherOwnsChild(teacher, notice.getChild());
    }

    private void validateTeacherOwnsChildId(User teacher, Long childId) {
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원아입니다."));
        validateTeacherOwnsChild(teacher, child);
    }

    @Transactional(readOnly = true)
    public List<NoticeListDto> getNoticeList(String email, LocalDate date) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        Classroom classroom = classrooms.get(0);
        List<Child> children = childRepository.findByClassroomId(classroom.getId());

        return children.stream().map(child -> {
            List<Notice> notices = noticeRepository.findAllByChildIdAndDate(child.getId(), date);
            Notice notice = null;
            if (!notices.isEmpty()) {
                notice = notices.stream()
                        .filter(n -> n.getStatus() == Notice.Status.DRAFT)
                        .findFirst()
                        .orElse(notices.get(notices.size() - 1));
            }

            List<ParentMemo> memos = parentMemoRepository.findAllByChildIdAndDate(child.getId(), date);
            ParentMemo memo = memos.isEmpty() ? null : memos.get(memos.size() - 1);

            return NoticeListDto.of(child, notice, memo);
        }).toList();
    }

    private Notice saveNoticeInternal(String email, NoticeRequestDto dto, Notice.Status status) throws IOException {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원아입니다."));

        validateTeacherOwnsChild(teacher, child);

        Notice notice = noticeRepository.findByChildIdAndDate(child.getId(), dto.getDate())
                .orElse(null);

        boolean isNew = (notice == null);

        if (notice != null) {
            notice.update(dto.getMeal(), dto.getPlay(), dto.getToilet(),
                    dto.getSpecial(), dto.getExtra(), dto.isAttended(), dto.getNap());
            notice.updateStatus(status);
        } else {
            notice = Notice.builder()
                    .date(dto.getDate())
                    .meal(dto.getMeal())
                    .play(dto.getPlay())
                    .toilet(dto.getToilet())
                    .special(dto.getSpecial())
                    .extra(dto.getExtra())
                    .attended(dto.isAttended())
                    .nap(dto.getNap())
                    .status(status)
                    .child(child)
                    .teacher(teacher)
                    .build();
            noticeRepository.save(notice);
        }

        if (dto.getPhotos() != null) {
            for (MultipartFile photo : dto.getPhotos()) {
                if (!photo.isEmpty()) {
                    String fileName = FileUploadUtils.store(photo, System.getProperty("user.dir"), uploadDir);
                    NoticePhoto noticePhoto = NoticePhoto.builder()
                            .filePath(fileName)
                            .notice(notice)
                            .build();
                    noticePhotoRepository.save(noticePhoto);
                }
            }
        }

        if (dto.getTeacherReply() != null && !dto.getTeacherReply().isBlank()) {
            NoticeReply reply = NoticeReply.builder()
                    .content(dto.getTeacherReply())
                    .notice(notice)
                    .author(teacher)
                    .build();
            noticeReplyRepository.save(reply);
        }

        // 신규 발행 시 학부모에게 알림 전송
        if (isNew && status == Notice.Status.PUBLISHED) {
            for (User parent : child.getParents()) {
                notificationService.sendNotification(parent,
                        child.getName() + "의 " + dto.getDate() + " 알림장이 등록되었습니다.");
            }
        }

        return notice;
    }

    @Transactional
    public void saveNotice(String email, NoticeRequestDto dto) throws IOException {
        saveNoticeInternal(email, dto, Notice.Status.PUBLISHED);
    }

    @Transactional
    public void saveDraft(String email, NoticeRequestDto dto) throws IOException {
        saveNoticeInternal(email, dto, Notice.Status.DRAFT);
    }

    @Transactional
    public void saveAllDraft(String email, String extra, LocalDate date) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return;

        Classroom classroom = classrooms.get(0);
        List<Child> children = childRepository.findByClassroomId(classroom.getId());

        for (Child child : children) {
            Notice notice = noticeRepository.findByChildIdAndDate(child.getId(), date)
                    .orElse(null);

            if (notice != null) {
                notice.updateExtra(extra);
                notice.updateStatus(Notice.Status.DRAFT);
            } else {
                notice = Notice.builder()
                        .date(date)
                        .extra(extra)
                        .attended(false)
                        .status(Notice.Status.DRAFT)
                        .child(child)
                        .teacher(teacher)
                        .build();
                noticeRepository.save(notice);
            }
        }
    }

    @Transactional
    public void updateAttended(String email, Long childId, LocalDate date, boolean attended) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        validateTeacherOwnsChildId(teacher, childId);

        Notice notice = noticeRepository.findByChildIdAndDate(childId, date)
                .orElse(null);
        if (notice != null) {
            notice.updateAttended(attended);
        }
    }

    @Transactional(readOnly = true)
    public List<Notice> getParentNoticeList(String email) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Child child = childRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("연결된 원아가 없습니다."));

        return noticeRepository.findByChildIdAndStatusOrderByDateDesc(child.getId(), Notice.Status.PUBLISHED);
    }

    @Transactional
    public NoticeDetailResult getNoticeDetail(String email, Long noticeId) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림장입니다."));

        validateParentOwnsNotice(parent, notice);

        notice.getPhotos().size();
        List<NoticeReply> replies = noticeReplyRepository.findWithAuthorByNoticeIdOrderByCreatedAtAsc(noticeId);

        if (notice.getNoticeRead() == null) {
            NoticeRead noticeRead = NoticeRead.builder()
                    .notice(notice)
                    .parent(parent)
                    .readAt(LocalDateTime.now())
                    .build();
            noticeReadRepository.save(noticeRead);
        }

        return new NoticeDetailResult(notice, replies);
    }

    @Transactional(readOnly = true)
    public Notice getNoticeDetailForTeacher(String email, Long noticeId) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림장입니다."));

        validateTeacherOwnsNotice(teacher, notice);

        return notice;
    }

    @Transactional
    public void saveReply(String email, Long noticeId, String content) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림장입니다."));

        validateParentOwnsNotice(parent, notice);

        NoticeReply reply = NoticeReply.builder()
                .content(content)
                .notice(notice)
                .author(parent)
                .build();

        noticeReplyRepository.save(reply);
    }

    @Transactional
    public NoticeDetailResult getNoticeDetailByDate(String email, Long childId, LocalDate date) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        List<Notice> notices = noticeRepository.findAllByChildIdAndDate(childId, date);
        Notice notice = notices.stream()
                .filter(n -> n.getStatus() == Notice.Status.PUBLISHED)
                .findFirst()
                .orElse(null);

        if (notice == null) return new NoticeDetailResult(null, List.of());

        validateParentOwnsNotice(parent, notice);

        notice.getPhotos().size();
        List<NoticeReply> replies = noticeReplyRepository.findWithAuthorByNoticeIdOrderByCreatedAtAsc(notice.getId());

        if (notice.getNoticeRead() == null) {
            NoticeRead noticeRead = NoticeRead.builder()
                    .notice(notice)
                    .parent(parent)
                    .readAt(LocalDateTime.now())
                    .build();
            noticeReadRepository.save(noticeRead);
        }

        return new NoticeDetailResult(notice, replies);
    }

    @Transactional(readOnly = true)
    public LocalDate getNoticeDateById(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .map(Notice::getDate)
                .orElse(LocalDate.now());
    }
}