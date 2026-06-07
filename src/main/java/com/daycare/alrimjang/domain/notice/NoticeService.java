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
            // 수정
            notice.update(dto.getMeal(), dto.getPlay(), dto.getToilet(),
                    dto.getSpecial(), dto.getExtra(), dto.isAttended());
        } else {
            // 새로 생성
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
                    // 절대 경로로 업로드 디렉토리 생성
                    Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }

                    // UUID로 파일명 중복 방지
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    Path filePath = uploadPath.resolve(fileName);

                    // transferTo 대신 Files.copy 사용
                    Files.copy(photo.getInputStream(), filePath);

                    // DB에 사진 경로 저장
                    NoticePhoto noticePhoto = NoticePhoto.builder()
                            .filePath(fileName)
                            .notice(notice)
                            .build();
                    noticePhotoRepository.save(noticePhoto);
                }
            }
        }
    }
}