package com.daycare.alrimjang.domain.eventreport;

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
public class EventReportService {

    private final EventReportRepository eventReportRepository;
    private final EventPhotoRepository eventPhotoRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 교사 - 행사보고 목록 조회
    @Transactional(readOnly = true)
    public List<EventReport> getTeacherEventReportList(String email) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return eventReportRepository.findByClassroomIdOrderByCreatedAtDesc(classrooms.get(0).getId());
    }

    // 학부모 - 행사보고 목록 조회
    @Transactional(readOnly = true)
    public List<EventReport> getParentEventReportList(String email) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return eventReportRepository.findByClassroomIdOrderByCreatedAtDesc(classroom.getId());
    }

    // 행사보고 상세 조회
    @Transactional(readOnly = true)
    public EventReport getEventReport(Long id) {
        return eventReportRepository.findWithPhotosById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 행사보고입니다."));
    }

    // 행사보고 등록
    @Transactional
    public void saveEventReport(String email, EventReportRequestDto dto) throws IOException {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) throw new IllegalArgumentException("담당 반이 없습니다.");

        EventReport eventReport = EventReport.builder()
                .title(dto.getTitle())
                .eventDate(dto.getEventDate())
                .content(dto.getContent())
                .classroom(classrooms.get(0))
                .teacher(teacher)
                .build();

        eventReportRepository.save(eventReport);

        // 사진 저장
        if (dto.getPhotos() != null) {
            for (MultipartFile photo : dto.getPhotos()) {
                if (!photo.isEmpty()) {
                    Path uploadPath = Paths.get(System.getProperty("user.dir"), uploadDir);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
                    Files.copy(photo.getInputStream(), uploadPath.resolve(fileName));

                    eventPhotoRepository.save(EventPhoto.builder()
                            .filePath(fileName)
                            .eventReport(eventReport)
                            .build());
                }
            }
        }
    }

    // 행사보고 수정
    @Transactional
    public void updateEventReport(Long id, EventReportRequestDto dto) {
        EventReport eventReport = eventReportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 행사보고입니다."));

        eventReport.update(dto.getTitle(), dto.getEventDate(), dto.getContent());
    }

    // 행사보고 삭제
    @Transactional
    public void deleteEventReport(Long id) {
        eventReportRepository.deleteById(id);
    }

    // 교사 - 행사보고 검색
    @Transactional(readOnly = true)
    public List<EventReport> searchTeacherEventReport(String email, String keyword) {
        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return eventReportRepository.searchByKeyword(classrooms.get(0).getId(), keyword);
    }

    // 학부모 - 행사보고 검색
    @Transactional(readOnly = true)
    public List<EventReport> searchParentEventReport(String email, String keyword) {
        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return eventReportRepository.searchByKeyword(classroom.getId(), keyword);
    }

    // 교사 - 기간별 조회
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