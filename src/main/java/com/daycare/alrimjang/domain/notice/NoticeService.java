package com.daycare.alrimjang.domain.notice;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.classroom.ClassroomRepository;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 교사의 담당 반 원아 목록 + 해당 날짜 알림장 여부 조회
    @Transactional(readOnly = true)
    public List<NoticeListDto> getNoticeList(String email, LocalDate date) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        Classroom classroom = classrooms.get(0);
        List<Child> children = childRepository.findByClassroomId(classroom.getId());

        return children.stream().map(child -> {
            Notice notice = noticeRepository.findByChildIdAndDate(child.getId(), date)
                    .orElse(null);
            return NoticeListDto.of(child, notice);
        }).toList();
    }

    // 알림장 저장 (작성 또는 수정)
    @Transactional
    public void saveNotice(String email, NoticeRequestDto dto) throws IOException {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        Child child = childRepository.findById(dto.getChildId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 원아입니다."));

        Notice notice = noticeRepository.findByChildIdAndDate(child.getId(), dto.getDate())
                .orElse(null);

        if (notice != null) {
            notice.update(dto.getMeal(), dto.getPlay(), dto.getToilet(),
                    dto.getSpecial(), dto.getExtra(), dto.isAttended());
        } else {
            notice = Notice.builder()
                    .date(dto.getDate())
                    .meal(dto.getMeal())
                    .play(dto.getPlay())
                    .toilet(dto.getToilet())
                    .special(dto.getSpecial())
                    .extra(dto.getExtra())
                    .attended(dto.isAttended())
                    .child(child)
                    .teacher(teacher)
                    .build();
            noticeRepository.save(notice);
        }

        // 사진 저장
        if (dto.getPhotos() != null) {
            for (MultipartFile photo : dto.getPhotos()) {
                if (!photo.isEmpty()) {
                    Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(photo.getInputStream(), filePath);

                    NoticePhoto noticePhoto = NoticePhoto.builder()
                            .filePath(fileName)
                            .notice(notice)
                            .build();
                    noticePhotoRepository.save(noticePhoto);
                }
            }
        }
    }

    // 학부모용 - 본인 아이 알림장 목록 조회
    @Transactional(readOnly = true)
    public List<Notice> getParentNoticeList(String email) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Child child = childRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("연결된 원아가 없습니다."));

        return noticeRepository.findByChildIdOrderByDateDesc(child.getId());
    }

    // 학부모용 - 알림장 상세 조회 + 읽음 처리
    @Transactional
    public Notice getNoticeDetail(String email, Long noticeId) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림장입니다."));

        // 읽음 처리
        if (notice.getNoticeRead() == null) {
            NoticeRead noticeRead = NoticeRead.builder()
                    .notice(notice)
                    .parent(parent)
                    .readAt(LocalDateTime.now())
                    .build();
            noticeReadRepository.save(noticeRead);
        }

        return notice;
    }

    // 학부모 답장 저장
    @Transactional
    public void saveReply(String email, Long noticeId, String content) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림장입니다."));

        NoticeReply reply = NoticeReply.builder()
                .content(content)
                .notice(notice)
                .parent(parent)
                .build();

        noticeReplyRepository.save(reply);
    }
}