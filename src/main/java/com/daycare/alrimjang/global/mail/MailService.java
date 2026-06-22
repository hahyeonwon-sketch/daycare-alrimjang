package com.daycare.alrimjang.global.mail;

import com.daycare.alrimjang.domain.notification.NotificationService;
import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 공통 메일 발송
    public void sendMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("메일 발송 실패: " + e.getMessage());
        }
    }

    // 공통 알림 발송 (DB저장 + SSE + 메일)
    public void sendNotification(String email, String subject, String text, String sseMessage) {
        // DB 저장 + SSE 실시간 알림
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            notificationService.sendNotification(user, sseMessage);
        }
        // 이메일 알림
        sendMail(email, subject, text);
    }

    // 학부모 승인 알림
    public void sendApprovalMail(String to, String name) {
        sendNotification(to,
                "[클로버 어린이집] 가입 승인 안내",
                name + " 학부모님, 가입이 승인되었습니다.\n\n클로버 어린이집 알림장 서비스를 이용하실 수 있습니다.",
                "가입이 승인되었습니다. 서비스를 이용하실 수 있습니다.");
    }

    // 알림장 작성 알림
    public void sendNoticeMail(String to, String parentName, String date) {
        sendNotification(to,
                "[클로버 어린이집] 알림장이 등록되었습니다",
                parentName + " 학부모님, " + date + " 알림장이 등록되었습니다.\n\n앱에서 확인해주세요.",
                date + " 알림장이 등록되었습니다.");
    }

    // 일정 전날 알림
    public void sendScheduleMail(String to, String parentName, String title, String date) {
        sendNotification(to,
                "[클로버 어린이집] 내일 일정 안내",
                parentName + " 학부모님, 내일(" + date + ") 일정이 있습니다.\n\n일정: " + title,
                "내일(" + date + ") 일정: " + title);
    }

    // 공지사항 등록 알림
    public void sendAnnouncementMail(String to, String parentName, String title) {
        sendNotification(to,
                "[클로버 어린이집] 공지사항이 등록되었습니다",
                parentName + " 학부모님, 새 공지사항이 등록되었습니다.\n\n제목: " + title,
                "새 공지사항: " + title);
    }

    // 행사보고 등록 알림
    public void sendEventReportMail(String to, String parentName, String title) {
        sendNotification(to,
                "[클로버 어린이집] 행사보고가 등록되었습니다",
                parentName + " 학부모님, 새 행사보고가 등록되었습니다.\n\n제목: " + title,
                "새 행사보고: " + title);
    }
}