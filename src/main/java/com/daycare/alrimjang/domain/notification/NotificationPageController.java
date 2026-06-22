package com.daycare.alrimjang.domain.notification;

import com.daycare.alrimjang.domain.user.User;
import com.daycare.alrimjang.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/parent/notifications")
@RequiredArgsConstructor
public class NotificationPageController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @GetMapping
    public String notificationList(@AuthenticationPrincipal UserDetails userDetails,
                                   Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        List<NotificationResponseDto> notifications = notificationService.getNotifications(user.getId());
        long unreadCount = notificationService.getUnreadCount(user.getId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);

        return "parent/notifications";
    }

    @PostMapping("/read-all")
    public String markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        notificationService.markAllAsRead(user.getId());
        return "redirect:/parent/notifications";
    }
}