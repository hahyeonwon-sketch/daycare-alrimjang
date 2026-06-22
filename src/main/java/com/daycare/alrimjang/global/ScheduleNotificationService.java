package com.daycare.alrimjang.global;

import com.daycare.alrimjang.domain.child.Child;
import com.daycare.alrimjang.domain.child.ChildRepository;
import com.daycare.alrimjang.domain.classroom.Classroom;
import com.daycare.alrimjang.domain.schedule.Schedule;
import com.daycare.alrimjang.domain.schedule.ScheduleRepository;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.global.mail.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleNotificationService {

    private final ScheduleRepository scheduleRepository;
    private final ChildRepository childRepository;
    private final MailService mailService;

    /**
     * 매일 오후 6시에 내일 일정 알림 발송.
     *
     * 수정: @Transactional(readOnly = true) 제거 → 기본 REQUIRED(읽기+쓰기) 트랜잭션 사용.
     * readOnly = true 안에서 NotificationService(쓰기)를 호출하면
     * MySQL Connector/J 가 "Connection is read-only" 에러를 낼 수 있었음.
     * 알림 발송 후 markAsNotified()로 중복 발송도 방지.
     */
    @Scheduled(cron = "0 0 18 * * *")
    @Transactional
    public void sendScheduleNotification() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        List<Schedule> schedules = scheduleRepository.findByDateAndNotifiedFalse(tomorrow);

        for (Schedule schedule : schedules) {
            Classroom classroom = schedule.getClassroom();
            List<Child> children = childRepository.findByClassroomId(classroom.getId());

            for (Child child : children) {
                for (User parent : child.getParents()) {
                    if (parent.isEmailNotification()) {
                        mailService.sendScheduleMail(
                                parent.getEmail(),
                                parent.getName(),
                                schedule.getTitle(),
                                tomorrow.toString()
                        );
                    }
                }
            }

            // 알림 발송 완료 표시 (더티체킹으로 UPDATE 실행됨)
            schedule.markAsNotified();
        }
    }
}