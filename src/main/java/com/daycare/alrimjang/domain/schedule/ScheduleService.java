package com.daycare.alrimjang.domain.schedule;

import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.classroom.ClassroomRepository;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    // 교사가 이 일정을 수정/삭제할 권한이 있는지 검증
    private void validateTeacherOwnsSchedule(User teacher, Schedule schedule) {
        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        boolean owns = classrooms.stream()
                .anyMatch(c -> c.getId().equals(schedule.getClassroom().getId()));
        if (!owns) {
            throw new IllegalArgumentException("본인 담당 반의 일정이 아닙니다.");
        }
    }

    // 교사 담당 반 일정 목록 조회
    @Transactional(readOnly = true)
    public List<Schedule> getScheduleList(String email) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) return List.of();

        return scheduleRepository.findByClassroomId(classrooms.get(0).getId());
    }

    // 학부모 소속 반 일정 목록 조회
    @Transactional(readOnly = true)
    public List<Schedule> getParentScheduleList(String email) {

        User parent = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 학부모입니다."));

        // 학부모 → 원아 → 반 조회
        Classroom classroom = classroomRepository.findByParentId(parent.getId())
                .orElseThrow(() -> new IllegalArgumentException("소속 반이 없습니다."));

        return scheduleRepository.findByClassroomId(classroom.getId());
    }

    // 일정 등록
    @Transactional
    public void saveSchedule(String email, ScheduleRequestDto dto) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        List<Classroom> classrooms = classroomRepository.findByTeacherId(teacher.getId());
        if (classrooms.isEmpty()) throw new IllegalArgumentException("담당 반이 없습니다.");

        Schedule schedule = Schedule.builder()
                .title(dto.getTitle())
                .date(dto.getDate())
                .endDate(dto.getEndDate())
                .content(dto.getContent())
                .notified(false)
                .classroom(classrooms.get(0))
                .build();

        scheduleRepository.save(schedule);
    }

    // 일정 수정
    @Transactional
    public void updateSchedule(String email, Long scheduleId, ScheduleRequestDto dto) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));

        validateTeacherOwnsSchedule(teacher, schedule);

        schedule.update(dto.getTitle(), dto.getDate(), dto.getEndDate(), dto.getContent());
    }

    // 일정 삭제
    @Transactional
    public void deleteSchedule(String email, Long scheduleId) {

        User teacher = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교사입니다."));

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 일정입니다."));

        validateTeacherOwnsSchedule(teacher, schedule);

        scheduleRepository.deleteById(scheduleId);
    }
}