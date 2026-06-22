package com.daycare.alrimjang.domain.announcement;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.classroom.ClassroomRepository;
import com.daycare.alrimjang.domain.notification.NotificationService;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import com.daycare.alrimjang.global.FileUploadUtils;
import com.daycare.alrimjang.global.HtmlSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;
    private final ClassroomRepository classroomRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private void validateTeacherOwnsAnnouncement(User teacher, Announcement announcement) {
        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        boolean owns = classrooms.stream()
                .anyMatch(c -> c.getId().equals(announcement.getClassroom().getId()));
        if (!owns) {
            throw new IllegalArgumentException("본인 담당 반의 공지사항이 아닙니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<Announcement> getTeacherAnnouncementList(String email) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return announcementRepository.findByClassroomIdOrderByCreatedAtDesc(classrooms.get(0).getId());
    }

    @Transactional(readOnly = true)
    public List<Announcement> getParentAnnouncementList(String email) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return announcementRepository.findByClassroomIdOrderByCreatedAtDesc(classroom.getId());
    }

    @Transactional(readOnly = true)
    public Announcement getAnnouncement(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));
    }

    @Transactional
    public void saveAnnouncement(String email, AnnouncementRequestDto dto) throws IOException {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) throw new IllegalArgumentException("담당 반이 없습니다.");

        String filePath = null;
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            filePath = FileUploadUtils.store(dto.getFile(), System.getProperty("user.dir"), uploadDir);
        }

        Announcement announcement = Announcement.builder()
                .title(dto.getTitle())
                .content(HtmlSanitizer.sanitize(dto.getContent()))
                .filePath(filePath)
                .classroom(classrooms.get(0))
                .teacher(teacher)
                .build();

        announcementRepository.save(announcement);

        // 반 학부모들에게 알림 발송
        List<Child> children = childRepository.findByClassroomId(classrooms.get(0).getId());
        for (Child child : children) {
            for (User parent : child.getParents()) {
                notificationService.sendNotification(parent,
                        "[공지] " + dto.getTitle() + " 공지사항이 등록되었습니다.");
            }
        }
    }

    @Transactional
    public void updateAnnouncement(String email, Long id, AnnouncementRequestDto dto) throws IOException {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        validateTeacherOwnsAnnouncement(teacher, announcement);

        String filePath = announcement.getFilePath();
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            filePath = FileUploadUtils.store(dto.getFile(), System.getProperty("user.dir"), uploadDir);
        }

        announcement.update(dto.getTitle(), HtmlSanitizer.sanitize(dto.getContent()), filePath);
    }

    @Transactional
    public void deleteAnnouncement(String email, Long id) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        Announcement announcement = announcementRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지사항입니다."));

        validateTeacherOwnsAnnouncement(teacher, announcement);

        announcementRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Announcement> searchTeacherAnnouncement(String email, String keyword) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return announcementRepository.searchByKeyword(classrooms.get(0).getId(), keyword);
    }

    @Transactional(readOnly = true)
    public List<Announcement> searchParentAnnouncement(String email, String keyword) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return announcementRepository.searchByKeyword(classroom.getId(), keyword);
    }

    @Transactional(readOnly = true)
    public List<Announcement> getTeacherAnnouncementByDate(String email, LocalDate startDate, LocalDate endDate) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return announcementRepository.findByClassroomIdAndDateBetween(
                classrooms.get(0).getId(),
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay());
    }
}