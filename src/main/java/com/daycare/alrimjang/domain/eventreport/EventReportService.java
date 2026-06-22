package com.daycare.alrimjang.domain.eventreport;

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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventReportService {

    private final EventReportRepository eventReportRepository;
    private final EventPhotoRepository eventPhotoRepository;
    private final ClassroomRepository classroomRepository;
    private final ChildRepository childRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private void validateTeacherOwnsEventReport(User teacher, EventReport eventReport) {
        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        boolean owns = classrooms.stream()
                .anyMatch(c -> c.getId().equals(eventReport.getClassroom().getId()));
        if (!owns) {
            throw new IllegalArgumentException("본인 담당 반의 행사보고가 아닙니다.");
        }
    }

    @Transactional(readOnly = true)
    public List<EventReport> getTeacherEventReportList(String email) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return eventReportRepository.findByClassroomIdOrderByCreatedAtDesc(classrooms.get(0).getId());
    }

    @Transactional(readOnly = true)
    public List<EventReport> getParentEventReportList(String email) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return eventReportRepository.findByClassroomIdOrderByCreatedAtDesc(classroom.getId());
    }

    @Transactional(readOnly = true)
    public EventReport getEventReport(Long id) {
        return eventReportRepository.findWithPhotosById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 행사보고입니다."));
    }

    @Transactional
    public void saveEventReport(String email, EventReportRequestDto dto) throws IOException {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) throw new IllegalArgumentException("담당 반이 없습니다.");

        EventReport eventReport = EventReport.builder()
                .title(dto.getTitle())
                .eventDate(dto.getEventDate())
                .content(HtmlSanitizer.sanitize(dto.getContent()))
                .classroom(classrooms.get(0))
                .teacher(teacher)
                .build();

        eventReportRepository.save(eventReport);

        if (dto.getPhotos() != null) {
            for (MultipartFile photo : dto.getPhotos()) {
                if (!photo.isEmpty()) {
                    String fileName = FileUploadUtils.store(photo, System.getProperty("user.dir"), uploadDir);
                    eventPhotoRepository.save(EventPhoto.builder()
                            .filePath(fileName)
                            .eventReport(eventReport)
                            .build());
                }
            }
        }

        // 반 학부모들에게 알림 발송
        List<Child> children = childRepository.findByClassroomId(classrooms.get(0).getId());
        for (Child child : children) {
            for (User parent : child.getParents()) {
                notificationService.sendNotification(parent,
                        "[행사보고] " + dto.getTitle() + " 행사보고가 등록되었습니다.");
            }
        }
    }

    @Transactional
    public void updateEventReport(String email, Long id, EventReportRequestDto dto) throws IOException {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        EventReport eventReport = eventReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 행사보고입니다."));

        validateTeacherOwnsEventReport(teacher, eventReport);

        eventReport.update(dto.getTitle(), dto.getEventDate(), HtmlSanitizer.sanitize(dto.getContent()));

        if (dto.getPhotos() != null) {
            for (MultipartFile photo : dto.getPhotos()) {
                if (!photo.isEmpty()) {
                    String fileName = FileUploadUtils.store(photo, System.getProperty("user.dir"), uploadDir);
                    eventPhotoRepository.save(EventPhoto.builder()
                            .filePath(fileName)
                            .eventReport(eventReport)
                            .build());
                }
            }
        }
    }

    @Transactional
    public void deleteEventReport(String email, Long id) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        EventReport eventReport = eventReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 행사보고입니다."));

        validateTeacherOwnsEventReport(teacher, eventReport);

        eventReportRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<EventReport> searchTeacherEventReport(String email, String keyword) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return eventReportRepository.searchByKeyword(classrooms.get(0).getId(), keyword);
    }

    @Transactional(readOnly = true)
    public List<EventReport> searchParentEventReport(String email, String keyword) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return eventReportRepository.searchByKeyword(classroom.getId(), keyword);
    }

    @Transactional(readOnly = true)
    public List<EventReport> getTeacherEventReportByDate(String email, LocalDate startDate, LocalDate endDate) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return eventReportRepository.findByClassroomIdAndDateBetween(
                classrooms.get(0).getId(), startDate, endDate);
    }
}