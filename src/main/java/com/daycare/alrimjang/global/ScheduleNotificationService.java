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

    // 매일 오후 6시에 내일 일정 알림 발송
    @Scheduled(cron = "0 0 18 * * *")
    @Transactional(readOnly = true)
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
        }
    }
}