package com.daycare.alrimjang.domain.announcement;

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
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 교사 - 공지사항 목록 조회
    @Transactional(readOnly = true)
    public List<Announcement> getTeacherAnnouncementList(String email) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return announcementRepository.findByClassroomIdOrderByCreatedAtDesc(classrooms.get(0).getId());
    }

    // 학부모 - 공지사항 목록 조회
    @Transactional(readOnly = true)
    public List<Announcement> getParentAnnouncementList(String email) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return announcementRepository.findByClassroomIdOrderByCreatedAtDesc(classroom.getId());
    }

    // 공지사항 상세 조회
    @Transactional(readOnly = true)
    public Announcement getAnnouncement(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
    }

    // 공지사항 등록
    @Transactional
    public void saveAnnouncement(String email, AnnouncementRequestDto dto) throws IOException {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) throw new IllegalArgumentException("담당 반이 없습니다.");

        // 파일 업로드 처리
        String filePath = null;
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + dto.getFile().getOriginalFilename();
            Files.copy(dto.getFile().getInputStream(), uploadPath.resolve(fileName));
            filePath = fileName;
        }

        Announcement announcement = Announcement.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .filePath(filePath)
                .classroom(classrooms.get(0))
                .teacher(teacher)
                .build();

        announcementRepository.save(announcement);
    }

    // 공지사항 수정
    @Transactional
    public void updateAnnouncement(Long id, AnnouncementRequestDto dto) throws IOException {
        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        String filePath = announcement.getFilePath();
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String fileName = UUID.randomUUID() + "_" + dto.getFile().getOriginalFilename();
            Files.copy(dto.getFile().getInputStream(), uploadPath.resolve(fileName));
            filePath = fileName;
        }

        announcement.update(dto.getTitle(), dto.getContent(), filePath);
    }

    // 공지사항 삭제
    @Transactional
    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }
}